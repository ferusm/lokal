plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

dependencies {
    api("com.squareup:kotlinpoet:1.12.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")

    testImplementation(kotlin("test"))
}