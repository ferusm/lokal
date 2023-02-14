# loKal

[![](https://jitpack.io/v/ferusm/lokal.svg)](https://jitpack.io/#ferusm/lokal)

This is compile-time localization tool for Kotlin

## Getting started

build.gradle.kts
```kotlin
plugins {
    kotlin("jvm") version "1.8.0"
    id("io.github.ferusm.lokal") version "0.5.1"
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
$version: 0.5.1
$summary: Text entries
$description: Status messages and etc.
message:
  $summary: Basic message
  $description: Basic message for test porpoises
  ~: Hello, comrade {name}
  ru: Привет, товарисч {name}
http:
  $version: 0.5.1
  $summary: Http text entries
  $description: Http status messages and etc.
  message:
    $summary: Basic message
    $description: Basic message for test porpoises
    ~: Hello, comrade {name}
    ru: Привет, товарисч {name}
  http:
    $version: 0.5.1
    $summary: Http text entries
    $description: Http status messages and etc.
    message:
      $summary: Basic message
      $description: Basic message for test porpoises
      ~: Hello, comrade {name}
      ru: Привет, товарисч {name}

```

## Help
See example project