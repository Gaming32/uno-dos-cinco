package io.github.gaming32.unodoscinco.network

import java.io.DataInputStream
import java.io.DataOutputStream

fun DataInputStream.readMcString(maxLength: Int): String {
    val length = readShort().toInt()
    if (length > maxLength) {
        throw IllegalArgumentException("String is too long! $length > $maxLength")
    }
    if (length < 0) {
        throw IllegalArgumentException("String length is less than zero! $length < 0")
    }
    return String(ByteArray(length).also(this::readFully), Charsets.UTF_16BE)
}

fun DataOutputStream.writeMcString(s: String) {
    if (s.length > Short.MAX_VALUE) {
        throw IllegalArgumentException("String length exceeds maximum! ${s.length} > ${Short.MAX_VALUE}")
    }
    writeShort(s.length)
    writeChars(s)
}
