package io.github.gaming32.unodoscinco.level.chunk

import io.github.gaming32.unodoscinco.level.BlockState

class LevelChunk(val x: Int, val z: Int) : Chunk() {
    private val sections = arrayOfNulls<BlockSection>(16)

    override fun getBlockId(x: Int, y: Int, z: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        val section = sections[y shr 4] ?: return 0
        return section.getBlockId(x and 0xf, y and 0xf, z and 0xf)
    }

    override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
        if (y !in 0..255) return
        getOrCreateSection(y).setBlockId(x and 0xf, y and 0xf, z and 0xf, id)
    }

    override fun getBlockMetadata(x: Int, y: Int, z: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        val section = sections[y shr 4] ?: return 0
        return section.getBlockMetadata(x and 0xf, y and 0xf, z and 0xf)
    }

    override fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int) {
        if (y !in 0..255) return
        getOrCreateSection(y).setBlockMetadata(x and 0xf, y and 0xf, z and 0xf, value)
    }

    override fun getBlockState(x: Int, y: Int, z: Int): BlockState {
        if (y !in 0..255) {
            return BlockState.Air
        }
        val section = sections[y shr 4] ?: return BlockState.Air
        return BlockState(
            section.getBlockId(x and 0xf, y and 0xf, z and 0xf),
            section.getBlockMetadata(x and 0xf, y and 0xf, z and 0xf)
        )
    }

    override fun setBlockState(x: Int, y: Int, z: Int, state: BlockState) {
        if (y !in 0..255) return
        val section = getOrCreateSection(y)
        section.setBlockId(x and 0xf, y and 0xf, z and 0xf, state.block)
        section.setBlockMetadata(x and 0xf, y and 0xf, z and 0xf, state.metadata)
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
