package io.github.gaming32.unodoscinco.network

import io.github.gaming32.unodoscinco.MinecraftServer
import io.github.gaming32.unodoscinco.network.listener.LoginPacketListener
import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.util.CloseGuardInputStream
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream

private val logger = KotlinLogging.logger {}

class ClientManager(val server: MinecraftServer, val socket: Socket) {
    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel()

    var listener: PacketListener = LoginPacketListener(this)

    private val sendLock = Mutex()
    private val sendChannel = Channel<Packet>(Channel.UNLIMITED)

    private var timeSinceRead = 0
    private var terminationReason: String? = null

    suspend fun runReceiver() = coroutineScope {
        try {
            while (!readChannel.isClosedForRead) {
                val packetId = readChannel.readByte().toUByte()
                timeSinceRead = 0
                val constructor = try {
                     PacketList.constructors[packetId]
                        ?: throw IllegalArgumentException("Unknown packet ID $packetId")
                } catch (e: IllegalArgumentException) {
                    if (packetId < 128.toUByte()) {
                        val data = ByteArray(packetId.toInt()).also { readChannel.readAvailable(it) }.decodeToString()
                        logger.info { "Modern packet received? $data" }
                    }
                    throw e
                }
                val packet = withContext(Dispatchers.IO) {
                    constructor(DataInputStream(CloseGuardInputStream(readChannel.toInputStream())))
                }
                if (packet.packetId != packetId) {
                    throw IllegalArgumentException(
                        "Read packet's ID did not match read packet ID. ${packet.packetId} != $packetId"
                    )
                }
                logger.debug { "Handling $packet with $listener" }
                packet.handle(listener)
            }
        } catch (e: Exception) {
            if (e !is ClosedReceiveChannelException && terminationReason == null) {
                logger.error(e) { "Exception in packet handling" }
                kickImmediately(e.toString())
            }
        } finally {
            (listener as? LoginPacketListener)?.let(server.loginClients::remove)
            terminate("disconnect.genericReason")
        }
    }

    suspend fun runSender() {
        try {
            while (true) {
                val packet = sendChannel.receive().toByteArray()
                sendLock.withLock {
                    writeChannel.writeFully(packet)
                    writeChannel.flush()
                }
            }
        } catch (e: Exception) {
            if (e !is ClosedWriteChannelException && e !is ClosedReceiveChannelException && terminationReason == null) {
                logger.error(e) { "Exception in packet sending" }
                // Can't kick!
                terminate("disconnect.genericReason")
            }
        }
    }

    fun tick() {
        if (++timeSinceRead > 1200) {
            terminate("disconnect.timeout")
        }
        terminationReason?.let(listener::onTerminate)
    }

    fun sendPacket(packet: Packet) {
        sendChannel.trySend(packet)
    }

    suspend fun sendPacketImmediately(packet: Packet) {
        val bytes = packet.toByteArray()
        sendLock.withLock {
            writeChannel.writeFully(bytes)
            writeChannel.flush()
        }
    }

    suspend fun kickImmediately(reason: String) = listener.onKick(reason)

    fun kick(reason: String) = server.networkingScope.launch { kickImmediately(reason) }

    fun terminate(reason: String) {
        if (socket.isClosed) return
        terminationReason = reason
        socket.close()
    }
}
