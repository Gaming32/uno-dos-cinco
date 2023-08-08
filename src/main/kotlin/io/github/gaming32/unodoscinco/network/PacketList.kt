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
        register(254) { StatusPacket }
        register(255, ::DisconnectPacket)
    }
}
