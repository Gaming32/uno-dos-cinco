package io.github.gaming32.unodoscinco.command

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer

object ConsoleOutputListener : CommandOutputListener {
    private val logger = KotlinLogging.logger {}

    private fun Component.display() = ANSIComponentSerializer.ansi().serialize(this)

    override fun info(message: () -> Component) = infoText { message().display() }

    override fun infoText(message: () -> String) = logger.info(message)

    override fun error(message: Component) = error(message.display())

    override fun errorText(message: String) = logger.error { message }
}
