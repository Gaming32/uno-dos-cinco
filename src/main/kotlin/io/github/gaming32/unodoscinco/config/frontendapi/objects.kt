package io.github.gaming32.unodoscinco.config.frontendapi

import io.github.gaming32.unodoscinco.level.BlockState
import io.github.gaming32.unodoscinco.world.TerrainType
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike

fun color(named: String) =
    if (named.startsWith("#")) {
        TextColor.fromCSSHexString(named)
    } else {
        NamedTextColor.NAMES.value(named)
    }

fun hsv(h: Float, s: Float, v: Float) = TextColor.color(HSVLike.hsvLike(h, s, v))

fun state(stateText: String) = BlockState.parse(stateText)

fun terrain(type: String) = TerrainType.byId[type] ?: throw IllegalArgumentException("Unknown terrain type $type")
