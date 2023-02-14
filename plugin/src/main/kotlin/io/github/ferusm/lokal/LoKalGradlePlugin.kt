package io.github.ferusm.lokal

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile


@Suppress("unused")
class LoKalGradlePlugin : Plugin<Project> {
    companion object {
        const val NAME = "loKal"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create(NAME, LoKalGradlePluginExtension::class.java)

        target.tasks.register(NAME, LoKalGradlePluginTask::class.java) {
            it.input = extension.input
            it.output = extension.output
            it.pack = extension.pack

            if (extension.output != null && extension.sourceSet != null) {
                extension.sourceSet!!.kotlin.srcDir(extension.output!!)
            }
        }

        target.tasks.named("compileKotlin", KotlinCompile::class.java) {
            it.dependsOn(NAME)
        }
    }
}