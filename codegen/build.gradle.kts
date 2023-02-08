plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

dependencies {
    api("com.squareup:kotlinpoet:1.12.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")

    testImplementation(kotlin("test"))
}