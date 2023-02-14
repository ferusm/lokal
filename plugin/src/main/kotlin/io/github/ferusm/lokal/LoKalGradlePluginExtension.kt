package io.github.ferusm.lokal

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File


abstract class LoKalGradlePluginExtension {
    var sourceSet: KotlinSourceSet? = null
    var output: File? = null
    var input: File? = null
    var pack: String? = null
}