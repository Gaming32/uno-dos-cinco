package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

abstract class Packet(val packetId: UByte) {
    companion object {
        fun packetIdUByte(packetId: Int) = packetId.also {
            require(it in 0..255) { "Packet ID must be in range 0..255" }
        }.toUByte()
    }

    protected constructor(packetId: Int) : this(packetIdUByte(packetId))

    abstract fun write(output: DataOutputStream)

    abstract suspend fun handle(listener: PacketListener)

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.write(packetId.toInt())
        write(dos)
        dos.close()
        return baos.toByteArray()
    }
}
