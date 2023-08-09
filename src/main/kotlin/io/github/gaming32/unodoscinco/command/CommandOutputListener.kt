package io.github.gaming32.unodoscinco.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/**
 * Key thing to note about this: implementations of this class may be called from any thread.
 * If they are not thread-safe, they should delegate to the `MinecraftServer.scheduleTask*` methods.
 */
interface CommandOutputListener {
    fun info(message: () -> Component)

    fun infoText(message: () -> String) = info { Component.text(message()) }

    fun error(message: Component) = info { message.color(NamedTextColor.RED) }

    fun errorText(message: String) = error(Component.text(message))
}
