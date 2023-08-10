package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

data class TimePacket(val time: Long) : Packet(4) {
    constructor(input: DataInputStream) : this(input.readLong())

    override fun write(output: DataOutputStream) {
        output.writeLong(time)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleTime(this)
}
