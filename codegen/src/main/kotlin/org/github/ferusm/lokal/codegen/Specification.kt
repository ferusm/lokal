package org.github.ferusm.lokal.codegen

import kotlinx.serialization.Serializable

@Serializable
data class Specification(
    val name: String,
    val data: Map<String, Map<String, String>> = emptyMap()
)