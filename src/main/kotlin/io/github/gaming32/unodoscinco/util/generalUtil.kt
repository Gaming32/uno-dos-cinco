package io.github.gaming32.unodoscinco.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.ComponentSerializer
import java.security.MessageDigest
import java.util.*

private val sha1 = MessageDigest.getInstance("SHA-1")

fun ByteArray.sha1(): ByteArray {
    sha1.reset()
    return sha1.digest(this)
}

fun offlineUuid(username: String): UUID = UUID.nameUUIDFromBytes("OfflinePlayer:$username".encodeToByteArray())

private val uuidRegex = "([0-9a-fA-F]{8})-?([0-9a-fA-F]{4})-?([0-9a-fA-F]{4})-?([0-9a-fA-F]{4})-?([0-9a-fA-F]+)".toRegex()

fun String.toUuid(): UUID = UUID.fromString(replace(uuidRegex, "$1-$2-$3-$4-$5"))

fun Component.append(text: String) = append(Component.text(text))

operator fun Component.plus(text: String) = append(text)

fun String.toComponent() = Component.text(this)

fun String.toComponent(color: TextColor) = Component.text(this, color)

fun <R : Any> Component.serialize(serializer: ComponentSerializer<Component, *, R>) = serializer.serialize(this)
