package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

data class GameEventPacket(val event: Int, val param: Int = 0) : Packet(70) {
    companion object {
        const val NO_RESPAWN_BLOCK_AVAILABLE = 0
        const val START_RAINING = 1
        const val STOP_RAINING = 2
        const val CHANGE_GAME_MODE = 3
        const val WIN_GAME = 4
    }

    constructor(input: DataInputStream) : this(input.readByte().toInt(), input.readByte().toInt())

    override fun write(output: DataOutputStream) {
        output.writeByte(event)
        output.writeByte(param)
    }

    override suspend fun handle(listener: PacketListener) = listener.handleGameEvent(this)
}
