package io.github.gaming32.unodoscinco.level.chunk.provider

import io.github.gaming32.unodoscinco.level.chunk.Chunk
import io.github.gaming32.unodoscinco.level.chunk.EmptyChunk

data class SameChunkProvider(private val chunk: Chunk?) : ChunkProvider() {
    companion object {
        val Null = SameChunkProvider(null)
        val Empty = SameChunkProvider(EmptyChunk)
    }

    override fun getChunk(x: Int, z: Int) = chunk
}
