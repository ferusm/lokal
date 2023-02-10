# loKal
This is compile-time localization tool for Kotlin

## Getting started

build.gradle.kts
```kotlin
plugins {
    kotlin("jvm") version "1.8.0"
    id("io.github.ferusm.lokal") version "0.1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

loKal {
    sourceSet = kotlin.sourceSets["main"]
    register {
        input = projectDir.resolve("translations/specification.yaml")
        pack = "local.schema.test.generating"
        output = buildDir.resolve("generated/main/kotlin")
    }
}
```

## Help
See example project