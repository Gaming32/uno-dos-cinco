package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.level.ServerLevel
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

    lateinit var thread: Thread
        private set

    val port = 25565 // TODO: replace me with proper config

    var running = true

    val loginClients = mutableSetOf<LoginPacketListener>()

    private val scheduledPacketTasks = LinkedBlockingQueue<() -> Unit>()
    private val scheduledTasks = LinkedBlockingQueue<TickTask>()

    private val _levels = mutableMapOf<Int, ServerLevel>()
    val levels: Map<Int, ServerLevel> get() = _levels

    val overworld get() = _levels.getValue(0)

    var tickCount = 0
        private set

    override fun run() {
        thread = Thread.currentThread()

        logger.info { "Server starting" }
        val startBeginTime = System.currentTimeMillis()

        timerHack()
        listen()
        initLevels()
        prepareLevel()
        readCommands()

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
            if (time - targetTickEnd > 500) {
                targetTickEnd = time + 100
            }
            tickCount++
        }

        logger.info { "Server stopped" }
    }

    private fun initLevels() {
        _levels[0] = ServerLevel()
        _levels[-1] = ServerLevel()
        _levels[1] = ServerLevel()
    }

    private fun prepareLevel() {
    }

    private fun runTick(targetTickEnd: Long) {
        loginClients.forEach(LoginPacketListener::tick)
        while (true) {
            val task = scheduledPacketTasks.poll() ?: break
            task()
        }

        // TODO: Actual ticking

        while (true) {
            val task = scheduledTasks.peek() ?: break
            if (System.currentTimeMillis() < targetTickEnd || tickCount - task.tick > 3) {
                task.action()
                scheduledTasks.remove()
            } else {
                break
            }
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
        scheduledTasks += TickTask(tickCount, task)
    }

    fun runOrScheduleTask(task: () -> Unit) {
        if (Thread.currentThread() == thread) {
            task()
        } else {
            scheduleTask(task)
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

    private data class TickTask(val tick: Int, val action: () -> Unit)
}

fun main(args: Array<String>) {
    val parser = ArgParser("uno-dos-cinco")
    parser.parse(args)

    MinecraftServer().run()
}
