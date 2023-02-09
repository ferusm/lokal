plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":codegen"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.8.10")
}


gradlePlugin {
    plugins {
        create("loKal") {
            id = "org.github.ferusm.lokal"
            displayName = "loKal"
            description = "Compile-time localization tool for Kotlin"
            implementationClass = "org.github.ferusm.lokal.LoKalGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ferusm/loKal"
    vcsUrl = "https://github.com/ferusm/loKal"
    tags = setOf("kotlin", "codegen", "localization", "text")
}