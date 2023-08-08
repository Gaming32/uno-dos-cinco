package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import java.io.DataInputStream
import java.io.DataOutputStream

data class BeginAuthPacket(val authId: String) : Packet(2) {
    constructor(input: DataInputStream) : this(input.readMcString(64))

    override fun write(output: DataOutputStream) {
        output.writeMcString(authId)
    }

    override fun handle(listener: PacketListener) = listener.handleBeginAuth(this)
}
