package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.StatusPacket

class LoginPacketListener(manager: ClientManager) : PacketListener(manager) {
    override suspend fun handleStatus(packet: StatusPacket) =
        manager.kickAsync("Config not implemented yet\u00a70\u00a720", false)

    fun tick() {
    }
}
