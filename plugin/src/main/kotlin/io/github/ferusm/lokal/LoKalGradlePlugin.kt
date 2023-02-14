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
            val input = extension.input ?: throw IllegalArgumentException("'input' parameter must be initialized with an spec file")
            val output = extension.output ?: throw IllegalArgumentException("'output' parameter must be initialized with an directory")
            val sourceSet = extension.sourceSet ?: throw IllegalArgumentException("'sourceSet' parameter must be initialized with an Kotlin sourceSet")
            val pack = extension.pack ?: throw IllegalArgumentException("'pack' parameter must be initialized with target package ref")

            if (output.isFile) {
                throw IllegalArgumentException("'output' must be a existent directory or points to desired directory location")
            }
            if (!output.exists() && output.mkdirs()) {
                throw IllegalArgumentException("Unable to create output dir with path ${output.absolutePath}")
            }

            if (!input.isFile) {
                throw IllegalArgumentException("input must be a file")
            }

            it.input = input
            it.output = output
            it.pack = pack

            sourceSet.kotlin.srcDir(output)
        }

        target.tasks.named("compileKotlin", KotlinCompile::class.java) {
            it.dependsOn(NAME)
        }
    }
}