import java.net.URI

plugins {
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

dependencies {
    api("com.squareup:kotlinpoet:1.12.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")

    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            from(components["kotlin"])
        }
    }
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