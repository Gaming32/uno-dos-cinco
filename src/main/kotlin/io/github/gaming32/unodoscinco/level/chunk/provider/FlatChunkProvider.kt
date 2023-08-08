package io.github.gaming32.unodoscinco.level.chunk.provider

import io.github.gaming32.unodoscinco.level.BlockState
import io.github.gaming32.unodoscinco.level.chunk.LevelChunk

data class FlatChunkProvider(val layers: List<BlockState> = listOf(
    // TODO: Use Blocks class
    BlockState(7),
    BlockState(3),
    BlockState(3),
    BlockState(2),
)) : ChunkProvider() {
    override fun getChunk(x: Int, z: Int) = LevelChunk(x, z).apply {
        layers.forEachIndexed { y, state ->
            repeat(16) { x ->
                repeat(16) { z ->
                    setBlockState(x, y, z, state)
                }
            }
        }
    }
}
