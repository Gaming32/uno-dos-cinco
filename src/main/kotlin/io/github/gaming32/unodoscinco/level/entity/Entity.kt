package io.github.gaming32.unodoscinco.level.entity

import io.github.gaming32.unodoscinco.level.ServerLevel
import io.github.gaming32.unodoscinco.util.Vec3
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.jvm.jvmName

abstract class Entity {
    companion object {
        private var nextId = 0
    }

    val id = nextId++

    open var uuid: UUID = UUID.randomUUID()
        protected set

    var position = Vec3()
        private set

    lateinit var level: ServerLevel

    fun moveTo(position: Vec3) {
        this.position = position
    }

    // TODO: Use entity type ID
    open val displayName: Component get() = Component.text(this::class.simpleName ?: this::class.jvmName)
}
