package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*

abstract class PacketListener(val manager: ClientManager) {
    private fun handleWithException(packet: Packet) {
        throw UnsupportedOperationException("${javaClass.simpleName} cannot handle ${packet.javaClass.simpleName}")
    }

    open fun handleKeepAlive(packet: KeepAlivePacket) = handleWithException(packet)

    open fun handleLogin(packet: LoginPacket) = handleWithException(packet)

    open fun handleBeginAuth(packet: BeginAuthPacket) = handleWithException(packet)

    open fun handleDisconnect(packet: DisconnectPacket) = handleWithException(packet)

    open fun handleStatus(packet: StatusPacket) = handleWithException(packet)
}
