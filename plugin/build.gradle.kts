plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":codegen"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.8.10")
}

java {
    withSourcesJar()
}

gradlePlugin {
    plugins {
        create("loKal") {
            id = "io.github.ferusm.lokal"
            displayName = "loKal"
            description = "Compile-time localization tool for Kotlin"
            implementationClass = "io.github.ferusm.lokal.LoKalGradlePlugin"
        }
    }
}