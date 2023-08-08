package io.github.gaming32.unodoscinco.level.chunk

import io.github.gaming32.unodoscinco.level.BlockState

class LevelChunk(val x: Int, val z: Int) : Chunk() {
    private val sections = arrayOfNulls<BlockSection>(16)

    override fun getBlockId(x: Int, y: Int, z: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        val section = sections[y shr 4] ?: return 0
        return section.getBlockId(x, y and 0xf, z)
    }

    override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
        if (y !in 0..255) return
        getOrCreateSection(y).setBlockId(x, y, z, id)
    }

    override fun getBlockMetadata(x: Int, y: Int, z: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        val section = sections[y shr 4] ?: return 0
        return section.getBlockMetadata(x, y and 0xf, z)
    }

    override fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int) {
        if (y !in 0..255) return
        getOrCreateSection(y).setBlockMetadata(x, y, z, value)
    }

    override fun getBlockState(x: Int, y: Int, z: Int): BlockState {
        if (y !in 0..255) {
            return BlockState.Air
        }
        val section = sections[y shr 4] ?: return BlockState.Air
        return BlockState(section.getBlockId(x, y and 0xf, z), section.getBlockMetadata(x, y and 0xf, z))
    }

    override fun setBlockState(x: Int, y: Int, z: Int, state: BlockState) {
        if (y !in 0..255) return
        val section = getOrCreateSection(y)
        section.setBlockId(x, y and 0xf, z, state.block)
        section.setBlockMetadata(x, y and 0xf, z, state.metadata)
    }

    private fun getOrCreateSection(y: Int): BlockSection {
        var section = sections[y shr 4]
        if (section == null) {
            section = BlockSection(y and 0xf0)
            sections[y shr 4] = section
        }
        return section
    }
}
