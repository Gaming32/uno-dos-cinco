package io.github.gaming32.unodoscinco.util

class NibbleArray(val size: Int) {
    init {
        require((size and 1) == 0) { "Size of NibbleArray must be a multiple of 2" }
    }

    private val data = ByteArray(size shr 1)

    operator fun get(nibbleIndex: Int): Int {
        // This weird ordering needs to be consistent with Vanilla for packets
        val byteIndex = nibbleIndex shr 1
        return if ((nibbleIndex and 1) == 0) {
            data[byteIndex].toInt() and 0xf
        } else {
            data[byteIndex].toInt() shr 4 and 0xf
        }
    }

    operator fun set(nibbleIndex: Int, value: Int) {
        val byteIndex = nibbleIndex shr 1
        if ((nibbleIndex and 1) == 0) {
            data[byteIndex] = ((data[byteIndex].toInt() and 0xf0) or (value and 0xf)).toByte()
        } else {
            data[byteIndex] = ((value and 0xf0) or (data[byteIndex].toInt() and 0xf)).toByte()
        }
    }
}
