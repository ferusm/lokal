package org.github.ferusm.lokal

import org.github.ferusm.lokal.codegen.Generator
import org.github.ferusm.lokal.codegen.Reader
import org.github.ferusm.lokal.codegen.Specification
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class LoKalGradlePluginTask : DefaultTask() {
    @Input
    lateinit var entries: Collection<LoKalGradlePluginEntry>

    @InputDirectory
    lateinit var output: File


    init {
        description = "LoKal codegen task"
    }

    @TaskAction
    fun run() {
        entries.forEach {
            if (!it.input.isFile) {
                throw IllegalArgumentException("${it.input.absolutePath} must be a file")
            }
            if (!output.exists() && !output.mkdirs()) {
                throw IllegalArgumentException("Unable to create output directory $output")
            }
            if (!output.exists() && !output.createNewFile()) {
                throw IllegalArgumentException("Unable to create output file $output")
            }
            val specification: Specification = if (it.input.isFile) {
                Reader.read(it.input)
            } else {
                throw IllegalArgumentException("${it.input} must be a file")
            }
            val fileSpec = Generator.generate(it.pack, specification)
            fileSpec.writeTo(output)
        }
    }
}