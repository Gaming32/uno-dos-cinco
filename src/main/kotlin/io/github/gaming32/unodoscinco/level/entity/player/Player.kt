package io.github.gaming32.unodoscinco.level.entity.player

import io.github.gaming32.unodoscinco.GameProfile
import io.github.gaming32.unodoscinco.level.entity.LivingEntity
import net.kyori.adventure.text.Component
import java.util.*

abstract class Player(val profile: GameProfile) : LivingEntity() {
    override var uuid: UUID
        get() = profile.id
        set(_) = throw UnsupportedOperationException("Cannot set Player.uuid")

    override val displayName: Component get() = Component.text(profile.name)
}
