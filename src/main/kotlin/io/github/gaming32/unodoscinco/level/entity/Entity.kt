package io.github.gaming32.unodoscinco.level.entity

import io.github.gaming32.unodoscinco.util.Vec3

abstract class Entity {
    companion object {
        private var nextId = 0
    }

    val id = nextId++

    private var position = Vec3()

    fun moveTo(position: Vec3) {
        this.position = position
    }
}
