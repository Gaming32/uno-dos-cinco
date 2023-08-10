package io.github.gaming32.unodoscinco.level.entity.player

import io.github.gaming32.unodoscinco.GameProfile
import io.github.gaming32.unodoscinco.MinecraftServer
import io.github.gaming32.unodoscinco.network.listener.ServerPlayPacketListener
import io.github.gaming32.unodoscinco.network.packet.ChatPacket.Companion.toChatPacket
import net.kyori.adventure.text.Component

class ServerPlayer(profile: GameProfile, val server: MinecraftServer) : Player(profile) {
    lateinit var connection: ServerPlayPacketListener

    var ping = 0

    fun chat(message: Component) = connection.manager.sendPacket(message.toChatPacket())

    suspend fun chatAsync(message: Component) = connection.manager.sendPacketAsync(message.toChatPacket())
}
