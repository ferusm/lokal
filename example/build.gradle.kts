plugins {
    kotlin("jvm") version "1.8.0"
    id("io.github.ferusm.lokal") version "0.5.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    mavenLocal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    testImplementation(kotlin("test"))
}

loKal {
    sourceSet = kotlin.sourceSets["main"]
    input = projectDir.resolve("translations/specification.yaml")
    pack = "local.schema.test.generating"
    output = buildDir.resolve("generated/main/kotlin")
}