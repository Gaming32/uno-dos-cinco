package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.DisconnectPacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.util.append
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

private val logger = KotlinLogging.logger {}

class ServerPlayPacketListener(manager: ClientManager, val player: ServerPlayer) : PacketListener(manager) {
    private var connectionClosed = false

    init {
        manager.listener = this
        player.connection = this
    }

    override suspend fun handleWithException(packet: Packet) {
        logger.warn { "${this::class} wasn't prepared to deal with a ${packet::class}" }
        manager.kickAsync("Protocol error, unexpected packet")
    }

    override suspend fun onKick(reason: String) {
        if (connectionClosed) return
        manager.server.playerList.allPlayers -= player
        manager.sendPacketAsync(DisconnectPacket(reason))
        manager.close()
        manager.server.sendChatAwait(
            Component.empty()
                .color(NamedTextColor.YELLOW)
                .append(player.displayName)
                .append(" left the game.")
        )
        connectionClosed = true
    }
}
