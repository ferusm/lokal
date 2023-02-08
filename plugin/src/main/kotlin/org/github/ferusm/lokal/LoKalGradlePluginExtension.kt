package org.github.ferusm.lokal

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject


abstract class LoKalGradlePluginExtension @Inject constructor(project: Project) {
    var sourceSet: KotlinSourceSet? = null
    var output: File = project.buildDir.resolve("generated/kotlin/main")

    val entries = mutableListOf<LoKalGradlePluginEntry>()

    @Suppress("unused")
    fun register(
        input: File,
        pack: String
    ) {
        val entry = LoKalGradlePluginEntry().apply {
            this.input = input
            this.pack = pack
        }
        entries.add(entry)
    }

    @Suppress("unused")
    fun register(body: LoKalGradlePluginEntry.() -> Unit) {
        val entry = LoKalGradlePluginEntry()
        body.invoke(entry)
        entry.validate()
        entries.add(entry)
    }
}