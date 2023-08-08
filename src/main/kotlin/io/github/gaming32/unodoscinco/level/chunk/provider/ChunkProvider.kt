package io.github.gaming32.unodoscinco.level.chunk.provider

import io.github.gaming32.unodoscinco.level.BlockPos
import io.github.gaming32.unodoscinco.level.chunk.Chunk

abstract class ChunkProvider {
    abstract fun getChunk(x: Int, z: Int): Chunk?

    fun getChunk(blockPos: BlockPos) = getChunk(blockPos.x shr 4, blockPos.z shr 4)
}
