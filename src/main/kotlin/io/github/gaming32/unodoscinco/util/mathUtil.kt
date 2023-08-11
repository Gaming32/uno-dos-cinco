package io.github.gaming32.unodoscinco.util

import kotlin.math.ceil
import kotlin.math.floor

fun wrapDegrees(value: Float) =
    if (value < -180f) {
        value - floor((value + 180) / 360f) * 360f
    } else if (value > 180f) {
        value - ceil((value - 180) / 360f) * 360f
    } else {
        value
    }
