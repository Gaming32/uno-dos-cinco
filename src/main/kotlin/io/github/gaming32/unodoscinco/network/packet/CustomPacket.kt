package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

data class CustomPacket(val channel: String, val length: Int, val data: ByteArray?) : Packet(250) {
    companion object {
        operator fun invoke(input: DataInputStream): CustomPacket {
            val channel = input.readMcString(16)
            val length = input.readShort().toInt()
            return CustomPacket(
                channel, length,
                if (length in 1..<32767) {
                    ByteArray(length).also(input::readFully)
                } else {
                    null
                }
            )
        }

        inline operator fun invoke(channel: String, action: DataOutputStream.() -> Unit): CustomPacket {
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            action(dos)
            dos.close()
            return CustomPacket(channel, baos.size(), baos.toByteArray())
        }
    }

    override fun write(output: DataOutputStream) {
        output.writeMcString(channel)
        output.writeShort(length)
        data?.let(output::write)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleCustom(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomPacket

        if (channel != other.channel) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channel.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
