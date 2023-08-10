package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

data class WorldSpawnPacket(val x: Int, val y: Int, val z: Int) : Packet(6) {
    constructor(input: DataInputStream) : this(input.readInt(), input.readInt(), input.readInt())

    override fun write(output: DataOutputStream) {
        output.writeInt(x)
        output.writeInt(y)
        output.writeInt(z)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleWorldSpawn(this)
}
