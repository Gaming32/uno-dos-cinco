package io.github.gaming32.unodoscinco.config.frontendapi

import io.github.gaming32.unodoscinco.level.BlockState
import io.github.gaming32.unodoscinco.world.TerrainType
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import kotlin.io.path.Path

fun color(named: String) =
    if (named.startsWith("#")) {
        TextColor.fromCSSHexString(named)
    } else {
        NamedTextColor.NAMES.value(named)
    }

fun hsv(h: Float, s: Float, v: Float) = TextColor.color(HSVLike.hsvLike(h, s, v))

fun state(stateText: String) = BlockState.parse(stateText)

fun terrain(type: String) = TerrainType.byId[type] ?: throw IllegalArgumentException("Unknown terrain type $type")

fun path(path: String) = Path(path)

fun path(path: String, vararg subpaths: String) = Path(path, *subpaths)

operator fun String.div(other: String) = Path(this, other)

fun regex(regex: String) = Regex(regex)

fun regex(regex: String, vararg options: RegexOption) = Regex(regex, options.toSet())
