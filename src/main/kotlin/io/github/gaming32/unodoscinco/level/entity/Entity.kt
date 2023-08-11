package io.github.gaming32.unodoscinco.level.entity

import io.github.gaming32.unodoscinco.level.ServerLevel
import io.github.gaming32.unodoscinco.util.Vec3
import io.github.gaming32.unodoscinco.util.wrapDegrees
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
    var yaw = 0f
    var pitch = 0f

    lateinit var level: ServerLevel

    fun moveTo(position: Vec3) {
        this.position = position
    }

    fun setPositionAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        position = Vec3(x, y, z)
        this.yaw = wrapDegrees(yaw)
        this.pitch = wrapDegrees(pitch)

        // TODO: Hitbox
    }

    val xPos get() = position.x
    val yPos get() = position.y
    val zPos get() = position.z

    // TODO: Use entity type ID
    open val displayName: Component get() = Component.text(this::class.simpleName ?: this::class.jvmName)
}
