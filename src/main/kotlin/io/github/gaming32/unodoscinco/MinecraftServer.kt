package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.network.ClientManager
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

    val commandQueue = LinkedBlockingQueue<String>()
    var running = true

    override fun run() {
        logger.info { "Starting server" }
        val startBeginTime = System.currentTimeMillis()

        timerHack()
        readCommands()
        listen()

        val startTotalTime = System.currentTimeMillis() - startBeginTime
        logger.info { "Server started in ${startTotalTime.milliseconds}" }

        while (running) {
            val startTick = System.currentTimeMillis()
            runTick()
            Thread.sleep(50 - System.currentTimeMillis() + startTick)
        }

        logger.info { "Stopping server" }
    }

    private fun runTick() {
        while (true) {
            val command = commandQueue.poll() ?: break
            if (command == "stop") {
                running = false
            } else {
                logger.info { "Unknown command \"$command\"" }
            }
        }
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
            commandQueue += readlnOrNull() ?: break
        }
    }

    private fun listen() = networkingScope.launch {
        aSocket(SelectorManager(Dispatchers.IO)).tcp().bind(port = port).use { serverSocket ->
            logger.info { "Listening on ${serverSocket.localAddress}" }
            while (true) {
                val socket = serverSocket.accept()
                runBlocking {
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
