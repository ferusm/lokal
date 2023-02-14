import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0" apply false
}

allprojects {
    group = "io.github.ferusm.lokal"
    version = "0.3.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io") {
            name = "JitPack"
        }
        mavenLocal()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}