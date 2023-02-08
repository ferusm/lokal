package org.github.ferusm.lokal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.github.ferusm.lokal.codegen.Generator
import org.github.ferusm.lokal.codegen.Specification
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


abstract class LoKalGradlePluginTask : DefaultTask() {
    @Input
    lateinit var entries: Collection<LoKalGradlePluginEntry>


    init {
        description = "LoKal codegen task"
    }

    @TaskAction
    @OptIn(ExperimentalSerializationApi::class)
    fun run() {
        entries.forEach {
            val input = project.projectDir.resolve(it.inputFilePath)
            if (!input.isFile) {
                throw IllegalArgumentException("${input.absolutePath} must be a file path")
            }

            val output = project.buildDir.resolve(it.outputDirPath)
            if (!output.exists() && !output.mkdirs()) {
                throw IllegalArgumentException("Unable to create output directory $output")
            }
            if (!output.exists() && !output.createNewFile()) {
                throw IllegalArgumentException("Unable to create output file $output")
            }

            val specification: Specification = if (input.isFile) {
                Json.decodeFromStream(input.inputStream())
            } else {
                throw IllegalArgumentException("${it.inputFilePath} must be a file path")
            }
            val fileSpec = Generator.generate(it.outputPackage, specification)
            fileSpec.writeTo(output)
        }
    }
}