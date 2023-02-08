plugins {
    `java-gradle-plugin`

    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":codegen"))
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
}

gradlePlugin {
    plugins {
        create("loKal") {
            id = "loKal"
            implementationClass = "org.github.ferusm.lokal.LoKalGradlePlugin"
        }
    }
}