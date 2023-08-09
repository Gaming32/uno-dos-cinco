package io.github.gaming32.unodoscinco.config

import io.github.gaming32.unodoscinco.util.sha1
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

@OptIn(ExperimentalStdlibApi::class)
object ConfigCompilationConfiguration : ScriptCompilationConfiguration({
    baseClass(ServerConfig::class)
    jvm {
        defaultImports(
            "io.github.gaming32.unodoscinco.*",
            "io.github.gaming32.unodoscinco.config.frontendapi.*",
            "net.kyori.adventure.extra.kotlin.*",
            "net.kyori.adventure.util.*",
            "net.kyori.adventure.text.*",
            "net.kyori.adventure.text.Component.*",
            "net.kyori.adventure.text.format.*",
            "net.kyori.adventure.text.format.TextColor.color",
        )
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    hostConfiguration(ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration) {
        jvm {
            compilationCache(CompiledScriptJarsCache { source, _ ->
                File("cache/config/${source.text.encodeToByteArray().sha1().toHexString()}.jar").also {
                    it.parentFile.mkdirs()
                }
            })
        }
    })
}) {
    private fun readResolve(): Any = ConfigCompilationConfiguration
}

object ConfigEvaluationConfiguration : ScriptEvaluationConfiguration({
    compilationConfiguration(ConfigCompilationConfiguration)
}) {
    private fun readResolve(): Any = ConfigEvaluationConfiguration
}

object ConfigScriptingHost : BasicJvmScriptingHost() {
    suspend fun evalSuspend(script: SourceCode) =
        compiler(script, ConfigCompilationConfiguration).onSuccess {
            evaluator(it, ConfigEvaluationConfiguration)
        }
}

suspend fun evalConfigFile(configFile: File) = ConfigScriptingHost
    .evalSuspend(configFile.toScriptSource())
    .valueOr { failure ->
        throw ConfigErrorException(failure.reports
            .asSequence()
            .filter(ScriptDiagnostic::isError)
            .joinToString("\n") { it.render().replace("\n", "\n    ") }
        )
    }
    .returnValue
    .scriptInstance as? ServerConfig
    ?: throw ConfigErrorException(
        "An unknown error occurred loading the config file. Try deleting the \"cache\" folder."
    )
