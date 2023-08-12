package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import io.github.gaming32.unodoscinco.util.serialize
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.io.DataInputStream
import java.io.DataOutputStream

data class ChatPacket(val message: String) : Packet(3) {
    companion object {
        const val MAX_LENGTH = 119

        fun Component.toChatPacket() = ChatPacket(serialize(LegacyComponentSerializer.legacySection()))
    }

    constructor(input: DataInputStream) : this(input.readMcString(MAX_LENGTH))

    override fun write(output: DataOutputStream) {
        output.writeMcString(message)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleChat(this)
}
