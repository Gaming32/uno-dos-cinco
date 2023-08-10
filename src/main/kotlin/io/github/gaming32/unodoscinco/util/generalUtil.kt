package io.github.gaming32.unodoscinco.util

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
