package io.github.gaming32.unodoscinco.level

data class BlockState(val block: Int, val metadata: Int = 0) {
    companion object {
        val Air = BlockState(0)
    }

    override fun toString() = "$block:$metadata" // TODO: Block name
}
