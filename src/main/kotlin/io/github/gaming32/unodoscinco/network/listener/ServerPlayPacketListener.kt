package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.DisconnectPacket
import io.github.gaming32.unodoscinco.network.packet.KeepAlivePacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.util.append
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class ServerPlayPacketListener(manager: ClientManager, val player: ServerPlayer) : PacketListener(manager) {
    private var connectionClosed = false

    private var tickCount = 0
    private var lastKeepAlive = 0
    private var lastKeepAliveTime = 0L
    private var pingId = 0

    private var spamCounter = 0

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
        manager.sendPacketImmediately(DisconnectPacket(reason))
        @Suppress("BlockingMethodInNonBlockingContext")
        manager.socket.close()
        manager.server.sendChat(
            Component.empty()
                .color(NamedTextColor.YELLOW)
                .append(player.displayName)
                .append(" left the game.")
        )
        manager.server.playerList.removePlayer(player)
        connectionClosed = true
    }

    override fun onTerminate(reason: String) {
        logger.info { "${printName()} lost connection: $reason" }
        manager.server.sendChat(
            Component.empty()
                .color(NamedTextColor.YELLOW)
                .append(player.displayName)
                .append(" left the game.")
        )
        manager.server.playerList.removePlayer(player)
        connectionClosed = true
    }

    override fun printName(): String {
        return player.profile.name
    }

    fun tick() {
        tickCount++
        manager.tick()

        if (tickCount - lastKeepAlive > 20) {
            lastKeepAlive = tickCount
            lastKeepAliveTime = System.nanoTime() / 1_000_000L
            pingId = Random.nextInt()
            manager.sendPacket(KeepAlivePacket(pingId))
        }

        if (spamCounter > 0) {
            spamCounter--
        }
    }
}
