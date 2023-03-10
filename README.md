# loKal

[![](https://jitpack.io/v/ferusm/lokal.svg)](https://jitpack.io/#ferusm/lokal)

This is compile-time localization tool for Kotlin

## Getting started

build.gradle.kts
```kotlin
plugins {
    kotlin("jvm") version "1.8.0"
    id("io.github.ferusm.lokal") version "0.7.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        name = "JitPack"
    }
}

loKal {
    sourceSet = kotlin.sourceSets["main"]
    input = projectDir.resolve("translations/specification.yaml")
    pack = "local.schema.test.generating"
    output = buildDir.resolve("generated/main/kotlin")
}
```

settings.gradle.kts
```kotlin
rootProject.name = "example"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io") {
            name = "JitPack"
        }
    }
}
```

translations/specification.yaml
```yaml
$version: 0.7.0
$summary: Example project text specification
$description: For test porpoise

firstMessage:
  $summary: Example project text
  $description: For test porpoise

  ~: Hello, first comrade {name}
  ru: Привет, первый товарисч {name}

secondMessage: Hello, second comrade {name}

firstGroup:
  $summary: Example project text group
  $description: For test porpoise

  firstMessage:
    $summary: Example project text
    $description: For test porpoise

    ~: Hello, first comrade {name}
    ru: Привет, первый товарисч {name}

  secondMessage: Hello, second comrade {name}

secondGroup:
  $summary: Another example project text group
  $description: For test porpoise

  someMessage:
    $summary: Another example project text
    $description: For test porpoise

    ~: Hello, second comrade {name}
    ru: Привет, второй товарисч {name}

  secondMessage: Hello, second comrade {name}
```

## Help
See example project