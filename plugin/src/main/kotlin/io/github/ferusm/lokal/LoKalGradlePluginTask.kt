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
    lateinit var input: File

    @Input
    lateinit var pack: String

    @OutputDirectory
    lateinit var output: File


    init {
        description = "LoKal codegen task"
    }

    @TaskAction
    fun run() {
        val specification = Reader.read(input)
        val fileSpec = Generator.generate(pack, specification)
        fileSpec.writeTo(output)
    }
}