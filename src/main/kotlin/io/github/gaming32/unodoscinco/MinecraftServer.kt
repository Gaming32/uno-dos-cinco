package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.listener.LoginPacketListener
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.cli.ArgParser
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

class MinecraftServer : Runnable {
    private var networkThreadId = 0
    val networkingScope = CoroutineScope(Executors.newCachedThreadPool {
        Thread(it, "Networking-" + networkThreadId++).apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher())

    val port = 25565 // TODO: replace me with proper config

    var running = true

    val loginClients = mutableSetOf<LoginPacketListener>()

    private val scheduledPacketTasks = LinkedBlockingQueue<() -> Unit>()
    private val scheduledTasks = LinkedBlockingQueue<() -> Unit>()

    override fun run() {
        logger.info { "Server starting" }
        val startBeginTime = System.currentTimeMillis()

        timerHack()
        readCommands()
        listen()

        val startTotalTime = System.currentTimeMillis() - startBeginTime
        logger.info { "Server started in ${startTotalTime.milliseconds}" }

        var targetTickEnd = System.currentTimeMillis() + 50
        while (running) {
            runTick(targetTickEnd)
            val time = System.currentTimeMillis()
            if (targetTickEnd > time) {
                Thread.sleep(targetTickEnd - time)
            }
            targetTickEnd += 50
        }

        logger.info { "Server stopping" }
    }

    private fun runTick(targetTickEnd: Long) {
        loginClients.forEach(LoginPacketListener::tick)
        while (true) {
            val task = scheduledPacketTasks.poll() ?: break
            task()
        }

        // TODO: Actual ticking

        while (System.currentTimeMillis() < targetTickEnd) {
            val task = scheduledTasks.poll() ?: break
            task()
        }
    }

    fun handleCommand(command: String, outputHandler: (() -> String) -> Unit) {
        if (command == "stop") {
            outputHandler { "Stopping server" }
            running = false
        } else {
            outputHandler { "Unknown command \"$command\"" }
        }
    }

    fun schedulePacketHandle(task: () -> Unit) {
        scheduledPacketTasks += task
    }

    fun scheduleTask(task: () -> Unit) {
        scheduledTasks += task
    }

    private fun timerHack() = thread(
        name = "TimerHack",
        isDaemon = true
    ) {
        Thread.sleep(Long.MAX_VALUE)
    }

    private fun readCommands() = thread(
        name = "CommandListener",
        isDaemon = true
    ) {
        while (true) {
            val command = readlnOrNull() ?: break
            scheduleTask {
                handleCommand(command, logger::info)
            }
        }
    }

    private fun listen() = networkingScope.launch {
        aSocket(SelectorManager(Dispatchers.IO)).tcp().bind(port = port).use { serverSocket ->
            logger.info { "Listening on ${serverSocket.localAddress}" }
            while (true) {
                val socket = serverSocket.accept()
                logger.info { "Received connection from ${socket.remoteAddress}" }
                launch {
                    ClientManager(this@MinecraftServer, socket).run()
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("uno-dos-cinco")
    parser.parse(args)

    MinecraftServer().run()
}
