package org.github.ferusm.lokal.codegen

import kotlinx.serialization.Serializable

@Serializable(SpecificationSerializer::class)
data class Specification(
    val groups: List<Group>
) {
    companion object {
        const val DEFAULT_KEY = "default"

        const val META_KEY = "\$meta"
        const val VERSION_KEY = "\$version"
        const val SUMMARY_KEY = "\$summary"
        const val DESCRIPTION_KEY = "\$description"
    }

    data class Group(
        val version: String? = null,
        val summary: String? = null,
        val description: String? = null,
        val name: String,
        val entries: List<Entry>
    )

    data class Entry(
        val summary: String? = null,
        val description: String? = null,
        val name: String,
        val default: String,
        val translations: Map<String, String>
    )
}