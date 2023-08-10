package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import java.io.DataInputStream
import java.io.DataOutputStream

data class TabListPacket(val username: String, val add: Boolean, val ping: Int) : Packet(201) {
    constructor(input: DataInputStream) : this(
        input.readMcString(16), input.readByte() != 0.toByte(), input.readShort().toInt()
    )

    override fun write(output: DataOutputStream) {
        output.writeMcString(username)
        output.writeByte(if (add) 1 else 0)
        output.writeShort(ping)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleTabList(this)
}
