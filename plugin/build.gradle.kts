import java.net.URI

plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":codegen"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.8.10")
}

gradlePlugin {
    plugins {
        create("loKal") {
            id = "io.github.ferusm.loKal"
            displayName = "loKal"
            description = "Compile-time localization tool for Kotlin"
            implementationClass = "io.github.ferusm.lokal.LoKalGradlePlugin"
        }
    }
}

publishing {
    publishing {
        repositories {
            maven {
                url = URI.create("https://maven.pkg.github.com/ferusm/loKal")
                credentials {
                    username = project.property("github.auth.user") as String
                    password = project.property("github.auth.token") as String
                }
            }
        }
    }
}