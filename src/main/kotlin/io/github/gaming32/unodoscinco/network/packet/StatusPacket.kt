package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataOutputStream

data object StatusPacket : Packet(254) {
    override fun write(output: DataOutputStream) = Unit

    override fun handle(listener: PacketListener) = listener.handleStatus(this)
}
