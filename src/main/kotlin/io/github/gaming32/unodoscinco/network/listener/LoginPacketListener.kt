package io.github.gaming32.unodoscinco.network.listener

import com.google.gson.JsonObject
import io.github.gaming32.unodoscinco.VersionInfo
import io.github.gaming32.unodoscinco.config.MotdCreationContext
import io.github.gaming32.unodoscinco.config.PingInfo
import io.github.gaming32.unodoscinco.network.ClientManager
import io.github.gaming32.unodoscinco.network.packet.*
import io.github.gaming32.unodoscinco.util.offlineUuid
import io.github.gaming32.unodoscinco.util.toUuid
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.future.await
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class LoginPacketListener(manager: ClientManager) : PacketListener(manager) {
    private data class LoginInfo(val username: String, val uuid: UUID)

    private lateinit var serverId: String
    private var tempUsername: String? = null
    private var loginInfo: LoginInfo? = null
    private var loginTimer = 0
    private val loginFuture = CompletableFuture<Unit>()

    override suspend fun handleWithException(packet: Packet) {
        logger.debug { "${this::class} wasn't prepared to deal with a ${packet::class}" }
        manager.kickAsync("Protocol error")
    }

    override suspend fun onKick(reason: String) = logger.info { "Disconnecting ${printName()}: $reason" }

    override suspend fun handleLogin(packet: LoginPacket) {
        if (tempUsername != null) {
            return manager.kickAsync("Cannot login multiple times!")
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
        loginInfo = if (manager.server.config.onlineMode) {
            val response = manager.server.httpClient.get {
                url("https://sessionserver.mojang.com/session/minecraft/hasJoined")
                parameter("username", packet.username)
                parameter("serverId", serverId)
            }
            if (response.status.value != 200) {
                return manager.kickAsync("Failed to verify username (${response.status.description})")
            }
            val body = response.body<JsonObject>()
            LoginInfo(
                body["name"].asString,
                body["id"].asString.toUuid()
            )
        } else {
            LoginInfo(packet.username, offlineUuid(packet.username))
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

        loginInfo?.let {
            loginInfo = null
            performLogin(it)
        }
    }

    private fun performLogin(loginInfo: LoginInfo) {
        logger.info { "Login success! $loginInfo" }
        // TODO: Implement
        loginFuture.complete(Unit) // Unblock packet handling
    }
}
