plugins {
    id("loKal")
    kotlin("jvm") version "1.8.0"
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
    register {
        inputFilePath = "translations/first.json"
        outputPackage = "local.schema.test.generating"
    }
}

tasks.named("compileKotlin") { dependsOn("loKal") }

kotlin.sourceSets["main"].kotlin {
    loKal {
        srcDir(defaultOutputDir)
    }
}