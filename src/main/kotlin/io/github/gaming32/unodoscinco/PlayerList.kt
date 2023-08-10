package io.github.gaming32.unodoscinco

import io.github.gaming32.unodoscinco.level.ServerLevel
import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.GameEventPacket
import io.github.gaming32.unodoscinco.network.packet.Packet
import io.github.gaming32.unodoscinco.network.packet.TabListPacket
import io.github.gaming32.unodoscinco.network.packet.TimePacket

class PlayerList(val server: MinecraftServer) {
    val allPlayers = mutableListOf<ServerPlayer>()

    fun broadcastPacket(packet: Packet, players: List<ServerPlayer> = allPlayers) = players.forEach {
        it.connection.manager.sendPacket(packet)
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

    fun removePlayer(player: ServerPlayer) {
        allPlayers -= player
        // TODO: Implement rest
    }
}
