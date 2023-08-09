package io.github.gaming32.unodoscinco.config

import io.github.gaming32.unodoscinco.MinecraftServer
import io.github.gaming32.unodoscinco.config.frontendapi.state
import io.github.gaming32.unodoscinco.level.BlockState
import io.github.gaming32.unodoscinco.level.entity.player.ServerPlayer
import io.ktor.network.sockets.*

data class PingInfo(val clientAddress: InetSocketAddress)

data class MotdCreationContext(val server: MinecraftServer, val pingInfo: PingInfo)

data class ChatFormatContext(val sender: ServerPlayer, val message: String)

data class BlockStateListContext(private val delegate: MutableList<BlockState>) : MutableList<BlockState> by delegate {
    operator fun String.unaryPlus() = +state(this)

    operator fun BlockState.unaryPlus() {
        delegate.add(this)
    }

    // TODO: +Block
}
