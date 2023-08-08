package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import java.io.DataInputStream
import java.io.DataOutputStream

data class DisconnectPacket(val reason: String) : Packet(255) {
    constructor(input: DataInputStream) : this(input.readMcString(256))

    override fun write(output: DataOutputStream) {
        output.writeMcString(reason)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleDisconnect(this)
}
