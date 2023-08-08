package io.github.gaming32.unodoscinco.level.chunk

import io.github.gaming32.unodoscinco.util.NibbleArray

class BlockSection(val minY: Int) {
    companion object {
        const val BLOCKS_PER_SECTION = 16 * 16 * 16

        // This weird ordering needs to be consistent with Vanilla for packets
        fun getBlockIndex(x: Int, y: Int, z: Int) = (y shl 8) or (z shl 4) or x
    }

    val blocks = ByteArray(BLOCKS_PER_SECTION)
    val metadata = NibbleArray(BLOCKS_PER_SECTION)
    val skylight = NibbleArray(BLOCKS_PER_SECTION)
    val blocklight = NibbleArray(BLOCKS_PER_SECTION)
    var blocksExt: NibbleArray? = null
        private set

    fun getBlockId(x: Int, y: Int, z: Int): Int {
        val index = getBlockIndex(x, y, z)
        var result = blocks[index].toInt() and 0xff
        blocksExt?.let { result = it[index] shl 8 or result }
        return result
    }

    fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
        val index = getBlockIndex(x, y, z)

        // TODO: Counters

        blocks[index] = (id and 0xff).toByte()
        if (index > 255 && blocksExt == null) {
            blocksExt = NibbleArray(BLOCKS_PER_SECTION)
        }
        blocksExt?.let { it[index] = id shr 8 }
    }

    fun getBlockMetadata(x: Int, y: Int, z: Int) = metadata[getBlockIndex(x, y, z)]

    fun setBlockMetadata(x: Int, y: Int, z: Int, value: Int) {
        metadata[getBlockIndex(x, y, z)] = value
    }
}
