package io.github.gaming32.unodoscinco.level.chunk

import io.github.gaming32.unodoscinco.level.BlockState

object EmptyChunk : Chunk() {
    override fun getBlockId(x: Int, y: Int, z: Int) = 0

    override fun setBlockId(x: Int, y: Int, z: Int, id: Int) = Unit

    override fun getBlockMetadata(x: Int, y: Int, z: Int) = 0

    override fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int) = Unit

    override fun getBlockState(x: Int, y: Int, z: Int) = BlockState.Air

    override fun setBlockState(x: Int, y: Int, z: Int, state: BlockState) = Unit
}
