package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.level.ServerLevel
import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.GameEventPacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.network.packet.TabListPacket
import io.github.gaming32.unodoscinco.network.packet.TimePacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

class PlayerList(val server: MinecraftServer) {
    val allPlayers = mutableListOf<ServerPlayer>()

    fun broadcastPacket(packet: Packet, players: List<ServerPlayer> = allPlayers): CompletableFuture<Unit> =
        CompletableFuture.allOf(
            *players.asSequence()
                .map { it.connection.manager.sendPacket(packet).asCompletableFuture() }
                .toList()
                .toTypedArray()
        ).thenApply {}

    suspend fun broadcastPacketAwait(packet: Packet, players: List<ServerPlayer> = allPlayers) = coroutineScope {
        broadcastPacketAsync(this, packet, players).joinAll()
    }

    suspend fun broadcastPacketAsync(
        scope: CoroutineScope, packet: Packet, players: List<ServerPlayer> = allPlayers
    ) = scope.run {
        players.asSequence()
            .map { launch { it.connection.manager.sendPacketAsync(packet) } }
            .toList()
    }

    fun makePlayer(manager: ClientManager, profile: GameProfile): ServerPlayer? {
        // TODO: Bans
        // TODO: Whitelist
        // TODO: IP bans
        if (allPlayers.size >= server.config.maxPlayers) {
            manager.kick("The server is full!")
            return null
        }
        allPlayers.asSequence()
            .filter { it.profile.id == profile.id || it.profile.name.equals(profile.name, true) }
            .forEach { it.connection.manager.kick("You logged in from another location") }
        return ServerPlayer(profile, server)
    }

    fun syncTimeAndWeather(player: ServerPlayer, level: ServerLevel) {
        player.connection.manager.sendPacket(TimePacket(0L)) // TODO: Time
        if (level.isRaining()) {
            player.connection.manager.sendPacket(GameEventPacket(GameEventPacket.START_RAINING))
        }
    }

    fun addPlayer(player: ServerPlayer) {
        broadcastPacket(TabListPacket(player.profile.name, true, 1000))
        allPlayers += player

        // TODO: Chunk loading

        broadcastPacket(TabListPacket(player.profile.name, true, player.ping))
    }
}
