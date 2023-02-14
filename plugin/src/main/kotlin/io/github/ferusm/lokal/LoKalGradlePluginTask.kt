package io.github.ferusm.lokal

import io.github.ferusm.lokal.codegen.Generator
import io.github.ferusm.lokal.codegen.Reader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class LoKalGradlePluginTask : DefaultTask() {
    @InputFile
    var input: File? = null

    @Input
    var pack: String? = null

    @OutputDirectory
    var output: File? = null


    init {
        description = "LoKal codegen task"
    }

    @TaskAction
    fun run() {
        val input = input ?: throw IllegalArgumentException("'input' parameter must be initialized with an spec file")
        val output = output ?: throw IllegalArgumentException("'output' parameter must be initialized with an directory")
        val pack = pack ?: throw IllegalArgumentException("'pack' parameter must be initialized with target package ref")

        if (output.isFile) {
            throw IllegalArgumentException("'output' must be a existent directory or points to desired directory location")
        }

        output.mkdirs()

        if (!input.isFile) {
            throw IllegalArgumentException("input must be a file")
        }

        val specification = Reader.read(input)
        val fileSpec = Generator.generate(pack, specification)
        fileSpec.writeTo(output)
    }
}