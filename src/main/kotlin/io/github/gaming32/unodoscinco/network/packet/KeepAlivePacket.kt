package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

data class KeepAlivePacket(val id: Int) : Packet(0) {
    constructor(input: DataInputStream) : this(input.readInt())

    override fun write(output: DataOutputStream) {
        output.writeInt(id)
    }

    override fun handle(listener: PacketListener) = listener.handleKeepAlive(this)
}
