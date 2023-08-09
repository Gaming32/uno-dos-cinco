package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.config.MotdCreationContext
import io.github.gaming32.unodoscinco.config.PingInfo
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.StatusPacket
import io.ktor.network.sockets.*

class LoginPacketListener(manager: ClientManager) : PacketListener(manager) {
    override suspend fun handleStatus(packet: StatusPacket) {
        val server = manager.server
        val config = server.config
        val motd = config.explicitMotd ?: config.motdGenerator(MotdCreationContext(
            server, PingInfo(manager.socket.remoteAddress as InetSocketAddress)
        ))
        manager.kickAsync(buildString {
            append(motd)
            append('\u00a7')
            append(0)
            append('\u00a7')
            append(config.maxPlayers)
        }, false)
    }

    fun tick() {
    }
}
