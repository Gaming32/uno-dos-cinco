package io.github.gaming32.unodoscinco.util

import java.security.MessageDigest

private val SHA1 = MessageDigest.getInstance("SHA-1")

fun ByteArray.sha1(): ByteArray {
    SHA1.reset()
    return SHA1.digest(this)
}
