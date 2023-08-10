plugins {
    kotlin("jvm") version "1.9.0"
}

group = "io.github.prcraftmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    api("it.unimi.dsi:fastutil-core:8.5.12")

    api("io.ktor:ktor-network:2.3.3")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-gson:2.3.3")

    api("io.github.oshai:kotlin-logging-jvm:5.0.1")
    api("org.slf4j:slf4j-api:2.0.7")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.9.0")

    api("net.kyori:adventure-api:4.14.0")
    api("net.kyori:adventure-extra-kotlin:4.14.0")
    api("net.kyori:adventure-text-serializer-ansi:4.14.0")
    api("net.kyori:adventure-text-serializer-legacy:4.14.0")
    api("net.kyori:adventure-text-serializer-plain:4.14.0")

    api("com.google.code.gson:gson:2.10.1")
}

kotlin {
    jvmToolchain(8)
}
