package io.github.gaming32.unodoscinco.level

data class BlockPos(val x: Int, val y: Int, val z: Int) {
    override fun toString() = "($x, $y, $z)"
}
