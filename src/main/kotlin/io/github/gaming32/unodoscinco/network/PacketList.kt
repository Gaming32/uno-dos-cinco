package io.github.gaming32.unodoscinco.network

import io.github.gaming32.unodoscinco.network.packet.*
import io.github.gaming32.unodoscinco.network.packet.Packet.Companion.packetIdUByte
import java.io.DataInputStream

object PacketList {
    private val _constructors = mutableMapOf<UByte, (DataInputStream) -> Packet>()
    val constructors: Map<UByte, (DataInputStream) -> Packet> get() = _constructors

    fun register(packetId: Int, constructor: (DataInputStream) -> Packet) {
        _constructors[packetIdUByte(packetId)] = constructor
    }

    init {
        register(0, ::KeepAlivePacket)
        register(1, ::LoginPacket)
        register(2, ::BeginAuthPacket)
        register(3, ::ChatPacket)
        register(4, ::TimePacket)
        register(6, ::WorldSpawnPacket)
        register(70, ::GameEventPacket)
        register(201, ::TabListPacket)
        register(202, ::PlayerAbilitiesPacket)
        register(250, CustomPacket::invoke)
        register(254) { StatusPacket }
        register(255, ::DisconnectPacket)
    }
}
