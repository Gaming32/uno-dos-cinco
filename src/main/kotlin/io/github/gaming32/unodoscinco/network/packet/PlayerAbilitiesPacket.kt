package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

data class PlayerAbilitiesPacket(
    val invulnerable: Boolean,
    val flying: Boolean,
    val allowFlying: Boolean,
    val isCreative: Boolean
) : Packet(202) {
    constructor(input: DataInputStream) : this(
        input.readBoolean(), input.readBoolean(), input.readBoolean(), input.readBoolean()
    )

    override fun write(output: DataOutputStream) {
        output.writeBoolean(invulnerable)
        output.writeBoolean(flying)
        output.writeBoolean(allowFlying)
        output.writeBoolean(isCreative)
    }

    override suspend fun handle(listener: PacketListener) = listener.handlePlayerAbilities(this)
}
