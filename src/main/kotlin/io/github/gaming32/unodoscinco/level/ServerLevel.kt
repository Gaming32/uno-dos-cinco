package io.github.gaming32.unodoscinco.level

import io.github.gaming32.unodoscinco.MinecraftServer
import io.github.gaming32.unodoscinco.level.chunk.EmptyChunk
import io.github.gaming32.unodoscinco.level.chunk.provider.ChunkProvider
import io.github.gaming32.unodoscinco.level.chunk.provider.FlatChunkProvider
import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import java.nio.file.Path
import kotlin.io.path.createDirectories

class ServerLevel(val server: MinecraftServer, val baseDir: Path) {
    val info = LevelInfo()
    val players = mutableListOf<ServerPlayer>()

    private val chunkProvider: ChunkProvider = FlatChunkProvider()

    init {
        info.terrainType = server.config.terrainType
        baseDir.createDirectories()
    }

    fun getChunk(x: Int, z: Int) = chunkProvider.getChunk(x, z) ?: EmptyChunk

    fun getChunk(pos: BlockPos) = chunkProvider.getChunk(pos) ?: EmptyChunk

    fun getBlockId(x: Int, y: Int, z: Int) = chunkProvider.getChunk(x shr 4, z shr 4)?.getBlockId(x, y, z) ?: 0

    fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
        chunkProvider.getChunk(x shr 4, z shr 4)?.setBlockId(x, y, z, id)
    }

    fun getBlockId(pos: BlockPos) = chunkProvider.getChunk(pos)?.getBlockId(pos) ?: 0

    fun setBlockId(pos: BlockPos, id: Int) {
        chunkProvider.getChunk(pos)?.setBlockId(pos, id)
    }

    fun getBlockMetadata(x: Int, y: Int, z: Int) =
        chunkProvider.getChunk(x shr 4, z shr 4)?.getBlockMetadata(x, y, z) ?: 0

    fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int) {
        chunkProvider.getChunk(x shr 4, z shr 4)?.setBlockMetadata(x, y, z, value)
    }

    fun getBlockMetadata(pos: BlockPos) = chunkProvider.getChunk(pos)?.getBlockMetadata(pos) ?: 0

    fun setBlockMetadata(pos: BlockPos, value: Int) {
        chunkProvider.getChunk(pos)?.setBlockMetadata(pos, value)
    }

    fun getBlockState(x: Int, y: Int, z: Int) =
        chunkProvider.getChunk(x shr 4, z shr 4)?.getBlockState(x, y, z) ?: BlockState.Air

    fun setBlockState(x: Int, y: Int, z: Int, state: BlockState) {
        chunkProvider.getChunk(x shr 4, z shr 4)?.setBlockState(x, y, z, state)
    }

    fun getBlockState(pos: BlockPos) = chunkProvider.getChunk(pos)?.getBlockState(pos) ?: BlockState.Air

    fun setBlockState(pos: BlockPos, state: BlockState) {
        chunkProvider.getChunk(pos)?.setBlockState(pos, state)
    }

    fun isRaining() = false // TODO: Raining
}
