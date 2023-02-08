plugins {
    kotlin("jvm") version "1.8.0"
    id("loKal")
}

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    testImplementation(kotlin("test"))
}

loKal {
    sourceSet = kotlin.sourceSets["main"]
    register {
        input = projectDir.resolve("translations/first.json")
        pack = "local.schema.test.generating"
        output = buildDir.resolve("generated/main/kotlin")
    }
}