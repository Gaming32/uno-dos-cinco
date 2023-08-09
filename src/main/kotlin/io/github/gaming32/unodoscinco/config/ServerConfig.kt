package io.github.gaming32.unodoscinco.config

import io.github.gaming32.unodoscinco.level.BlockState
import io.github.gaming32.unodoscinco.world.TerrainType
import net.kyori.adventure.text.Component
import kotlin.script.experimental.annotations.KotlinScript

typealias MotdCreator = suspend MotdCreationContext.() -> String

typealias ChatFormatter = suspend ChatFormatContext.() -> Component

@KotlinScript(
    fileExtension = "udc.kts",
    compilationConfiguration = ConfigCompilationConfiguration::class,
    evaluationConfiguration = ConfigEvaluationConfiguration::class
)
abstract class ServerConfig {
    internal object PreConfig : ServerConfig()

    var serverIp: String = "0.0.0.0"
        protected set(value) {
            field = value.ifEmpty { "0.0.0.0" }
        }

    var serverPort: Int = 25565
        protected set(value) {
            require(value in 0..65535) { "Invalid port: $value" }
            field = value
        }

    var viewDistance: Int = 10
        protected set

    var simulationDistance: Int = 10
        protected set

    var maxPlayers: Int = 20
        protected set

    var seed: Long? = null
        protected set

    var terrainType: TerrainType = TerrainType.DEFAULT
        protected set

    var flatLayers: List<BlockState> = listOf(
        BlockState(7),
        BlockState(3),
        BlockState(3),
        BlockState(2),
    )
        protected set

    protected inline fun flatLayers(action: BlockStateListContext.() -> Unit) {
        flatLayers = buildList { action(BlockStateListContext(this)) }
    }

    var autosavePeriod: Int = 5 * 60 * 20
        protected set

    internal var explicitMotd: String? = "My uno-dos-cinco server"
        private set

    protected var motd
        get() = explicitMotd ?: throw IllegalStateException("Cannot get explicit motd after setting motd generator")
        set(motd) {
            explicitMotd = motd
            motdGenerator = { explicitMotd!! }
        }

    var motdGenerator: MotdCreator = { explicitMotd!! }
        private set

    protected fun motd(generator: MotdCreator) {
        explicitMotd = null
        motdGenerator = generator
    }

    var chatFormatter: ChatFormatter = { Component.text(message) }
        private set

    protected fun formatChat(formatter: ChatFormatter) {
        chatFormatter = formatter
    }
}
