package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*

abstract class PacketListener(val manager: ClientManager) {
    protected abstract suspend fun handleWithException(packet: Packet)

    abstract suspend fun onKick(reason: String)

    open suspend fun handleKeepAlive(packet: KeepAlivePacket) = handleWithException(packet)

    open suspend fun handleLogin(packet: LoginPacket) = handleWithException(packet)

    open suspend fun handleBeginAuth(packet: BeginAuthPacket) = handleWithException(packet)

    open suspend fun handleDisconnect(packet: DisconnectPacket) = handleWithException(packet)

    open suspend fun handleStatus(packet: StatusPacket) = handleWithException(packet)

    open fun printName() = manager.socket.remoteAddress.toString()

    protected fun mainThread(action: () -> Unit) = manager.server.schedulePacketHandle(action)
}
