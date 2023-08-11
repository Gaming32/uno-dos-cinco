package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*
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

    private var hasMoved = false
    private var lastSafeX = 0.0
    private var lastSafeY = 0.0
    private var lastSafeZ = 0.0

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

    override suspend fun handleKeepAlive(packet: KeepAlivePacket) {
        if (packet.id != pingId) return
        val roundTripTime = (System.nanoTime() / 1_000_000L - lastKeepAliveTime).toInt()
        player.ping = (player.ping * 3 + roundTripTime) / 4
    }

    override suspend fun handleMovement(packet: MovementPacket) {
        // TODO: Implement
    }

    override suspend fun handleCustom(packet: CustomPacket) {
        val rep = when {
            packet.data == null -> "null"
            packet.length <= 24 -> "'${packet.data.decodeToString(0, packet.length)}'"
            else -> "'${packet.data.decodeToString(0, 24)}'... (length ${packet.length})"
        }
        logger.info { "Received custom packet '${packet.channel}' ($rep) from ${printName()}" }
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

    fun teleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        hasMoved = false
        lastSafeX = x
        lastSafeY = y
        lastSafeZ = z
        player.setPositionAndRotation(x, y, z, yaw, pitch)
        manager.sendPacket(MovementPacket.PositionRotation(x, y + 1.62, y, z, yaw, pitch, false))
    }
}
