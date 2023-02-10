plugins {
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

dependencies {
    api("com.squareup:kotlinpoet:1.12.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")

    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("codegen") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            from(components["java"])
        }
    }
}