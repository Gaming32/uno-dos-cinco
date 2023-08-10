package io.github.gaming32.unodoscinco.network.listener

import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*

abstract class PacketListener(val manager: ClientManager) {
    protected abstract suspend fun handleWithException(packet: Packet)

    abstract suspend fun onKick(reason: String)

    abstract fun onTerminate(reason: String)

    open suspend fun handleKeepAlive(packet: KeepAlivePacket) = handleWithException(packet)

    open suspend fun handleLogin(packet: LoginPacket) = handleWithException(packet)

    open suspend fun handleBeginAuth(packet: BeginAuthPacket) = handleWithException(packet)

    open suspend fun handleChat(packet: ChatPacket) = handleWithException(packet)

    open suspend fun handleTime(packet: TimePacket) = handleWithException(packet)

    open suspend fun handleWorldSpawn(packet: WorldSpawnPacket) = handleWithException(packet)

    open suspend fun handleGameEvent(packet: GameEventPacket) = handleWithException(packet)

    open suspend fun handleTabList(packet: TabListPacket) = handleWithException(packet)

    open suspend fun handlePlayerAbilities(packet: PlayerAbilitiesPacket) = handleWithException(packet)

    open suspend fun handleCustom(packet: CustomPacket) = handleWithException(packet)

    open suspend fun handleStatus(packet: StatusPacket) = handleWithException(packet)

    open suspend fun handleDisconnect(packet: DisconnectPacket) = handleWithException(packet)

    open fun printName() = manager.socket.remoteAddress.toString()

    protected fun mainThread(action: () -> Unit) = manager.server.schedulePacketHandle(action)
}
