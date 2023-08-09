package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.config.MotdCreationContext
import io.github.gaming32.unodoscinco.config.PingInfo
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.BeginAuthPacket
import io.github.gaming32.unodoscinco.network.packet.DisconnectPacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.network.packet.StatusPacket
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.sockets.*
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class LoginPacketListener(manager: ClientManager) : PacketListener(manager) {
    override suspend fun handleWithException(packet: Packet) {
        logger.debug { "${this::class} wasn't prepared to deal with a ${packet::class}" }
        manager.kickAsync("Protocol error")
    }

    override suspend fun onKick(reason: String) = logger.info { "Disconnecting ${printName()}: $reason" }

    override suspend fun handleBeginAuth(packet: BeginAuthPacket) {
        manager.sendPacketAsync(BeginAuthPacket(
            if (manager.server.config.onlineMode) {
                Random.nextLong().toString(16)
            } else {
                "-"
            }
        ))
    }

    override suspend fun handleStatus(packet: StatusPacket) {
        val server = manager.server
        val config = server.config
        val motd = config.explicitMotd ?: config.motdGenerator(MotdCreationContext(
            server, PingInfo(manager.socket.remoteAddress as InetSocketAddress)
        ))
        manager.sendPacketAsync(DisconnectPacket(buildString {
            append(motd)
            append('\u00a7')
            append(0)
            append('\u00a7')
            append(config.maxPlayers)
        }))
        manager.close()
    }

    fun tick() {
    }
}
