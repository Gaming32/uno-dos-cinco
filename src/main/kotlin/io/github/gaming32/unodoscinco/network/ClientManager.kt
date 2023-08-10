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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.DataInputStream

private val logger = KotlinLogging.logger {}

class ClientManager(val server: MinecraftServer, val socket: Socket) : AutoCloseable by socket {
    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel()

    var listener: PacketListener = LoginPacketListener(this)

    private val sendLock = Mutex()

    suspend fun run() = coroutineScope {
        try {
            while (!readChannel.isClosedForRead) {
                val packetId = readChannel.readByte().toUByte()
                val constructor = PacketList.constructors[packetId]
                    ?: throw IllegalArgumentException("Unknown packet ID $packetId")
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
            if (e !is ClosedReceiveChannelException && e !is ClosedSendChannelException) {
                logger.debug(e) { "Exception in packet handling" }
                kickAsync(e.toString())
            }
        } finally {
            (listener as? LoginPacketListener)?.let(server.loginClients::remove)
        }
    }

    private suspend fun send(bytes: ByteArray) = sendLock.withLock {
        writeChannel.writeFully(bytes)
        writeChannel.flush()
    }

    suspend fun sendPacketAsync(packet: Packet) = send(packet.toByteArray())

    fun sendPacket(packet: Packet): Job {
        val bytes = packet.toByteArray()
        return server.networkingScope.launch { send(bytes) }
    }

    suspend fun kickAsync(reason: String) = listener.onKick(reason)

    fun kick(reason: String) = server.networkingScope.launch { kickAsync(reason) }

    val isClosed get() = socket.isClosed
}
