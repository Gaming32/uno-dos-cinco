package io.github.gaming32.unodoscinco.util

import java.text.DecimalFormat

data class Vec3(val x: Double, val y: Double, val z: Double) {
    companion object {
        private val toStringFormat = DecimalFormat("0.00")
    }

    constructor() : this(0.0, 0.0, 0.0)

    override fun toString() =
        "Vec3(${toStringFormat.format(x)}, ${toStringFormat.format(y)}, ${toStringFormat.format(z)})"
}
