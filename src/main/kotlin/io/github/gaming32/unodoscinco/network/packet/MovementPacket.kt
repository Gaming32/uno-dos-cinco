package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.network.listener.PacketListener
import java.io.DataInputStream
import java.io.DataOutputStream

open class MovementPacket(val onGround: Boolean, val id: Int = 10) : Packet(id) {
    open val x get() = 0.0
    open val y get() = 0.0
    open val z get() = 0.0
    open val stance get() = 0.0

    open val yaw get() = 0f
    open val pitch get() = 0f

    open val hasPosition get() = false
    open val hasRotation get() = false

    constructor(input: DataInputStream) : this(input.readBoolean())

    override fun write(output: DataOutputStream) {
        output.writeBoolean(onGround)
    }

    final override suspend fun handle(listener: PacketListener) = listener.handleMovement(this)

    class Position(
        override val x: Double,
        override val y: Double,
        override val stance: Double,
        override val z: Double,
        onGround: Boolean
    ) : MovementPacket(onGround, 11) {
        override val hasPosition get() = true

        constructor(input: DataInputStream) : this(
            input.readDouble(),
            input.readDouble(),
            input.readDouble(),
            input.readDouble(),
            input.readBoolean()
        )

        override fun write(output: DataOutputStream) {
            output.writeDouble(x)
            output.writeDouble(y)
            output.writeDouble(stance)
            output.writeDouble(z)
            super.write(output)
        }
    }

    class Rotation(
        override val yaw: Float,
        override val pitch: Float,
        onGround: Boolean
    ) : MovementPacket(onGround, 12) {
        override val hasRotation get() = true

        constructor(input: DataInputStream) : this(input.readFloat(), input.readFloat(), input.readBoolean())

        override fun write(output: DataOutputStream) {
            output.writeFloat(yaw)
            output.writeFloat(pitch)
            super.write(output)
        }
    }

    class PositionRotation(
        override val x: Double,
        override val y: Double,
        override val stance: Double,
        override val z: Double,
        override val yaw: Float,
        override val pitch: Float,
        onGround: Boolean
    ) : MovementPacket(onGround, 13) {
        override val hasPosition get() = true
        override val hasRotation get() = true

        constructor(input: DataInputStream) : this(
            input.readDouble(),
            input.readDouble(),
            input.readDouble(),
            input.readDouble(),
            input.readFloat(),
            input.readFloat(),
            input.readBoolean()
        )

        override fun write(output: DataOutputStream) {
            output.writeDouble(x)
            output.writeDouble(y)
            output.writeDouble(stance)
            output.writeDouble(z)
            output.writeFloat(yaw)
            output.writeFloat(pitch)
            super.write(output)
        }
    }
}
