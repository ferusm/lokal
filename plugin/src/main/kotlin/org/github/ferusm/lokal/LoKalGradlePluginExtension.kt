package org.github.ferusm.lokal

import org.gradle.api.Project
import javax.inject.Inject


abstract class LoKalGradlePluginExtension @Inject constructor(project: Project) {
    val defaultOutputDir = "${project.buildDir.resolve("generated/main/kotlin")}"

    val entries = mutableListOf<LoKalGradlePluginEntry>()

    @Suppress("unused")
    fun register(
        inputFilePath: String,
        outputPackage: String,
        outputDirPath: String = defaultOutputDir
    ) {
        val entry = LoKalGradlePluginEntry().apply {
            this.inputFilePath = inputFilePath
            this.outputPackage = outputPackage
            this.outputDirPath = outputDirPath
        }
        entries.add(entry)
    }

    @Suppress("unused")
    fun register(body: LoKalGradlePluginEntry.() -> Unit) {
        val entry = LoKalGradlePluginEntry()
        body.invoke(entry)
        entry.validate(defaultOutputDir)
        entries.add(entry)
    }
}