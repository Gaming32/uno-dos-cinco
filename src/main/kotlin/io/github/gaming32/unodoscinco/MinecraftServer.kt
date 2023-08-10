package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.command.CommandContext
import io.github.gaming32.unodoscinco.command.ConsoleOutputListener
import io.github.gaming32.unodoscinco.config.ConfigErrorException
import io.github.gaming32.unodoscinco.config.ServerConfig
import io.github.gaming32.unodoscinco.config.evalConfigFile
import io.github.gaming32.unodoscinco.level.ServerLevel
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.listener.LoginPacketListener
import io.github.gaming32.unodoscinco.network.packet.ChatPacket.Companion.toChatPacket
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.serialization.gson.*
import kotlinx.cli.ArgParser
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

class MinecraftServer : Runnable {
    private var networkThreadId = 0
    val networkingScope = CoroutineScope(Executors.newCachedThreadPool {
        Thread(it, "Networking-" + networkThreadId++).apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher())

    val httpClient = HttpClient(CIO) {
        defaultRequest {
            userAgent("uno-dos-cinco/${VersionInfo.UNO_DOS_CINCO}")
        }
        install(ContentNegotiation) {
            gson()
        }
    }

    val configFile = Path("config.udc.kts")
    var config: ServerConfig = ServerConfig.PreConfig
        private set

    lateinit var mainThread: Thread
        private set
    private var running = true

    internal val loginClients = CopyOnWriteArraySet<LoginPacketListener>()
    val playerList = PlayerList(this)

    private val scheduledPacketTasks = LinkedBlockingQueue<() -> Unit>()
    private val scheduledTasks = LinkedBlockingQueue<TickTask<*>>()

    private val levels = mutableMapOf<Int, ServerLevel>()

    val overworld get() = levels.getValue(0)

    var tickCount = 0
        private set

    override fun run() {
        mainThread = Thread.currentThread()

        logger.info { "Server starting" }
        val startBeginTime = System.currentTimeMillis()

        try {
            runBlocking { reloadConfig() }
        } catch (e: ConfigErrorException) {
            return
        }
        logger.info { "Loaded config" }

        if (!config.onlineMode) {
            logger.warn { "*** SERVER IS IN OFFLINE MODE" }
            logger.warn { "*** Players can log in as whoever they chose." }
            logger.warn { "*** It is recommended to set onlineMode to true in config.udc.kts." }
            logger.warn { "*** Unlike Vanilla, we backport modern auth, so there is no reason to use offline mode." }
        }

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

    fun getLevel(id: Int) = levels[id]
        ?: throw IllegalArgumentException("Unknown dimension id $id")

    private fun initLevels() {
        levels[0] = ServerLevel(this)
        levels[-1] = ServerLevel(this)
        levels[1] = ServerLevel(this)
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

    fun handleCommand(command: String, context: CommandContext) {
        when (command) {
            "stop" -> {
                context.listener.infoText { "Stopping server" }
                running = false
            }
            "reload" -> {
                context.listener.infoText { "Reloading config" }
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    val start = System.currentTimeMillis()
                    try {
                        reloadConfig(false)
                    } catch (e: ConfigErrorException) {
                        return@launch context.listener.errorText(
                            "Failed to reload config:\n" + e.message?.prependIndent("   ")
                        )
                    }
                    val time = System.currentTimeMillis() - start
                    context.listener.infoText { "Reloaded config in ${time.milliseconds}" }
                }
            }
            else -> context.listener.errorText("Unknown command \"$command\"")
        }
    }

    fun schedulePacketHandle(task: () -> Unit) {
        scheduledPacketTasks += task
    }

    fun scheduleTask(force: Boolean = false, task: () -> Unit) {
        if (!force && Thread.currentThread() == mainThread) {
            return task()
        }
        scheduledTasks += TickTask(tickCount, task, null)
    }

    fun <T> scheduleTaskFuture(force: Boolean = false, task: () -> T): CompletableFuture<T> {
        if (!force && Thread.currentThread() == mainThread) {
            return CompletableFuture.completedFuture(task())
        }
        val future = CompletableFuture<T>()
        scheduledTasks += TickTask(tickCount, task, future)
        return future
    }

    fun <T> scheduleTaskAndWait(task: () -> T): T = scheduleTaskFuture(false, task).join()

    suspend fun <T> scheduleTaskAndWaitAsync(task: () -> T): T = scheduleTaskFuture(false, task).await()

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
                handleCommand(command, CommandContext(ConsoleOutputListener))
            }
        }
    }

    private fun listen() = networkingScope.launch {
        aSocket(SelectorManager(Dispatchers.IO)).tcp().bind(config.serverIp, config.serverPort).use { serverSocket ->
            logger.info { "Listening on ${serverSocket.localAddress}" }
            while (true) {
                val socket = serverSocket.accept()
                logger.info { "Received connection from ${socket.remoteAddress}" }
                val manager = ClientManager(this@MinecraftServer, socket)
                loginClients += manager.listener as LoginPacketListener
                launch { manager.runReceiver() }
                launch { manager.runSender() }
            }
        }
    }

    suspend fun reloadConfig(printError: Boolean = true) {
        if (!configFile.isRegularFile()) {
            javaClass.getResourceAsStream("/config.udc.kts")?.use { Files.copy(it, configFile) }
        }
        try {
            config = evalConfigFile(configFile.toFile())
        } catch (e: ConfigErrorException) {
            if (printError) {
                logger.error { "Failed to read config:\n" + e.message?.prependIndent("   ") }
            }
            throw e
        }
    }

    private fun printChat(message: Component) {
        logger.info { "[CHAT]: " + ANSIComponentSerializer.ansi().serialize(message) }
    }

    fun sendChat(message: Component) {
        printChat(message)
        playerList.broadcastPacket(message.toChatPacket())
    }

    private data class TickTask<T>(val tick: Int, val action: () -> T, val future: CompletableFuture<T>?)
}

fun main(args: Array<String>) {
    val parser = ArgParser("uno-dos-cinco")
    parser.parse(args)

    MinecraftServer().run()
}
