rootProject.name = "example"

pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = java.net.URI.create("https://maven.pkg.github.com/ferusm/loKal")
        }
    }
}