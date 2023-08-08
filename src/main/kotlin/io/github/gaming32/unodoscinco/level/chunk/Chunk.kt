package io.github.gaming32.unodoscinco.level.chunk

import io.github.gaming32.unodoscinco.level.BlockPos
import io.github.gaming32.unodoscinco.level.BlockState

abstract class Chunk {
    abstract fun getBlockId(x: Int, y: Int, z: Int): Int

    abstract fun setBlockId(x: Int, y: Int, z: Int, id: Int)

    abstract fun getBlockMetadata(x: Int, y: Int, z: Int): Int

    abstract fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int)

    open fun getBlockState(x: Int, y: Int, z: Int) = BlockState(getBlockId(x, y, z), getBlockMetadata(x, y, z))

    open fun setBlockState(x: Int, y: Int, z: Int, state: BlockState) {
        setBlockId(x, y, z, state.block)
        setBlockMetadata(x, y, z, state.metadata)
    }

    open fun getBlockId(pos: BlockPos) = getBlockId(pos.x, pos.y, pos.z)

    open fun setBlockId(pos: BlockPos, id: Int) = setBlockId(pos.x, pos.y, pos.z, id)

    open fun getBlockMetadata(pos: BlockPos) = getBlockMetadata(pos.x, pos.y, pos.z)

    open fun setBlockMetadata(pos: BlockPos, value: Int) = setBlockMetadata(pos.x, pos.y, pos.z, value)

    open fun getBlockState(pos: BlockPos) = getBlockState(pos.x, pos.y, pos.z)

    open fun setBlockState(pos: BlockPos, state: BlockState) = setBlockState(pos.x, pos.y, pos.z, state)
}
