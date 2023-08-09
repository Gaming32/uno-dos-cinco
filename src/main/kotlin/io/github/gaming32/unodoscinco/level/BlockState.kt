package io.github.gaming32.unodoscinco.level

data class BlockState(val block: Int, val metadata: Int = 0) {
    companion object {
        val Air = BlockState(0)

        fun parse(str: String): BlockState {
            val parts = str.trimStart('#').split(':', limit = 2)
            return BlockState(parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0)
        }
    }

    override fun toString() = "#$block:$metadata" // TODO: Block name
}
