package io.github.gaming32.unodoscinco.util

import java.io.FilterInputStream
import java.io.InputStream

class CloseGuardInputStream(delegate: InputStream) : FilterInputStream(delegate) {
    override fun close() = Unit
}
