package org.github.ferusm.lokal

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class LoKalGradlePlugin : Plugin<Project> {
    companion object {
        const val NAME = "loKal"
    }
    override fun apply(target: Project) {
        val extension = target.extensions.create(NAME, LoKalGradlePluginExtension::class.java)

        target.tasks.register(NAME, LoKalGradlePluginTask::class.java) {
            it.entries = extension.entries
        }
    }
}