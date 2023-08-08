package io.github.gaming32.unodoscinco.network

import io.github.gaming32.unodoscinco.MinecraftServer
import io.github.gaming32.unodoscinco.network.listener.LoginPacketListener
import io.github.gaming32.unodoscinco.network.packet.DisconnectPacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.util.CloseGuardInputStream
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.DataInputStream

private val logger = KotlinLogging.logger {}

class ClientManager(val server: MinecraftServer, val socket: Socket) {
    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel()

    var listener = LoginPacketListener(this)

    private val sendLock = Mutex()

    suspend fun run() = coroutineScope {
        try {
            while (!readChannel.isClosedForRead) {
                val packetId = readChannel.readByte().toUByte()
                val constructor = PacketList.constructors[packetId]
                    ?: throw IllegalArgumentException("Unknown packet ID $packetId")
                withContext(Dispatchers.IO) {
                    constructor(DataInputStream(CloseGuardInputStream(readChannel.toInputStream())))
                }.handle(listener)
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception in packet handling" }
            sendPacketAsync(DisconnectPacket(e.toString()))
            writeChannel.close()
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
}
