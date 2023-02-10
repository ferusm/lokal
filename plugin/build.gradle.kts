plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":codegen"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.8.10")
}