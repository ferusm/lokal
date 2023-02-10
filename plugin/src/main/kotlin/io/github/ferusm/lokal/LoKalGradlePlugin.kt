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
            extension.output.mkdirs()

            it.entries = extension.entries
            it.output = extension.output

            val sourceSet = extension.sourceSet
            if (sourceSet == null) {
                target.logger.warn("LoKal target sourceSet is undefined")
            } else {
                sourceSet.kotlin.srcDir(extension.output)
            }
        }

        target.tasks.named("compileKotlin", KotlinCompile::class.java) {
            it.dependsOn(NAME)
        }
    }
}