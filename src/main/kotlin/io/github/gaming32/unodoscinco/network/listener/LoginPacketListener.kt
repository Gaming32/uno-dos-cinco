package io.github.gaming32.unodoscinco.network.listener

import com.google.gson.JsonObject
import io.github.gaming32.unodoscinco.GameProfile
import io.github.gaming32.unodoscinco.VersionInfo
import io.github.gaming32.unodoscinco.config.MotdCreationContext
import io.github.gaming32.unodoscinco.config.PingInfo
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*
import io.github.gaming32.unodoscinco.util.append
import io.github.gaming32.unodoscinco.util.offlineUuid
import io.github.gaming32.unodoscinco.util.toUuid
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class LoginPacketListener(manager: ClientManager) : PacketListener(manager) {
    private lateinit var serverId: String
    private var tempUsername: String? = null
    private var profile: GameProfile? = null
    private var loginTimer = 0
    private val loginFuture = CompletableFuture<Unit>()

    override suspend fun handleWithException(packet: Packet) {
        logger.debug { "${this::class} wasn't prepared to deal with a ${packet::class}" }
        manager.kickAsync("Protocol error")
    }

    override suspend fun onKick(reason: String) {
        logger.info { "Disconnecting ${printName()}: $reason" }
        manager.sendPacketAsync(DisconnectPacket(reason))
        manager.close()
    }

    override suspend fun handleLogin(packet: LoginPacket) {
        if (tempUsername != null) {
            return manager.kickAsync("Cannot login multiple times!")
        }
        if (!packet.username.matches(manager.server.config.usernameRegex)) {
            return manager.kickAsync("Invalid username")
        }
        tempUsername = packet.username
        if (packet.protocolVersion != VersionInfo.PROTOCOL) {
            return manager.kickAsync(
                "This server only supports 1.2.5 clients. You are using " +
                    if (packet.protocolVersion > VersionInfo.PROTOCOL) {
                        "a newer"
                    } else {
                        "an older"
                    } + " client than is supported."
            )
        }
        profile = if (manager.server.config.onlineMode) {
            val response = manager.server.httpClient.get {
                url("https://sessionserver.mojang.com/session/minecraft/hasJoined")
                parameter("username", packet.username)
                parameter("serverId", serverId)
            }
            if (response.status.value != 200) {
                return manager.kickAsync("Failed to verify username")
            }
            val body = response.body<JsonObject>()
            GameProfile(
                body["name"].asString,
                body["id"].asString.toUuid()
            )
        } else {
            GameProfile(packet.username, offlineUuid(packet.username))
        }
        loginFuture.await() // Wait until logged in so that any subsequent packets are held off until the play phase
    }

    override suspend fun handleBeginAuth(packet: BeginAuthPacket) {
        serverId = if (manager.server.config.onlineMode) {
            Random.nextLong().toString(16)
        } else {
            "-"
        }
        manager.sendPacketAsync(BeginAuthPacket(serverId))
    }

    override suspend fun handleStatus(packet: StatusPacket) {
        val server = manager.server
        val config = server.config
        val motd = config.explicitMotd ?: config.motdGenerator(MotdCreationContext(
            server, PingInfo(manager.socket.remoteAddress as InetSocketAddress)
        ))
        manager.sendPacketAsync(DisconnectPacket(buildString {
            append(motd)
            append('\u00a7')
            append(0)
            append('\u00a7')
            append(config.maxPlayers)
        }))
        manager.close()
    }

    override fun printName(): String {
        if (tempUsername == null) {
            return super.printName()
        }
        return "$tempUsername [${super.printName()}]"
    }

    fun tick() {
        if (++loginTimer > 600) {
            manager.kick("Took too long to log in")
            return
        }

        profile?.let {
            profile = null
            performLogin(it)
        }
    }

    private fun performLogin(profile: GameProfile) {
        val playerList = manager.server.playerList
        val player = playerList.makePlayer(manager, profile)
        if (player != null) {
            // TODO: Read player data
            val level = manager.server.getLevel(0)
            player.level = level
            logger.info {
                "${printName()} logged in with entity id ${player.id} at " +
                    "(${player.position.x}, ${player.position.y}, ${player.position.z})"
            }
            val playHandler = ServerPlayPacketListener(manager, player)
            manager.sendPacket(LoginPacket(
                player.id,
                "",
                level.info.terrainType,
                1, // TODO: Gamemodes
                0, // TODO: Dimensions
                0.toUByte(), // TODO: Difficulty
                256.toUByte(),
                manager.server.config.maxPlayers.toUByte()
            ))
            manager.sendPacket(CustomPacket("MC|Brand") {
                write("uno-dos-cinco".encodeToByteArray())
            })
            manager.sendPacket(WorldSpawnPacket(0, 0, 0)) // TODO: World spawn
            manager.sendPacket(PlayerAbilitiesPacket(true, true, true, true)) // TODO: Player abilities
            playerList.syncTimeAndWeather(player, level)
            manager.server.sendChat(
                Component.empty()
                    .color(NamedTextColor.YELLOW)
                    .append(player.displayName)
                    .append(" joined the game.")
            )
            playerList.addPlayer(player)
            // TODO: Teleport
            manager.sendPacket(TimePacket(0)) // TODO: Time
            // TODO: Status effects
            // TODO: Crafting
        }
        loginFuture.complete(Unit) // Unblock packet handling
    }
}
