package io.github.gaming32.unodoscinco.network.packet

import io.github.gaming32.unodoscinco.level.TerrainType
import io.github.gaming32.unodoscinco.network.listener.PacketListener
import io.github.gaming32.unodoscinco.network.readMcString
import io.github.gaming32.unodoscinco.network.writeMcString
import java.io.DataInputStream
import java.io.DataOutputStream

data class LoginPacket(
    val protocolVersion: Int,
    val username: String,
    val terrainType: TerrainType,
    val gamemode: Int,
    val dimension: Int,
    val difficulty: UByte,
    val worldHeight: UByte,
    val maxPlayers: UByte
) : Packet(1) {
    constructor(input: DataInputStream) : this(
        input.readInt(),
        input.readMcString(16),
        TerrainType.byId[input.readMcString(16)] ?: TerrainType.DEFAULT,
        input.readInt(),
        input.readInt(),
        input.readByte().toUByte(),
        input.readByte().toUByte(),
        input.readByte().toUByte()
    )

    override fun write(output: DataOutputStream) {
        output.writeInt(protocolVersion)
        output.writeMcString(username)
        output.writeMcString(terrainType.id)
        output.writeInt(gamemode)
        output.writeInt(dimension)
        output.writeByte(difficulty.toInt())
        output.writeByte(worldHeight.toInt())
        output.writeByte(maxPlayers.toInt())
    }

    override suspend fun handle(listener: PacketListener) = listener.handleLogin(this)
}
