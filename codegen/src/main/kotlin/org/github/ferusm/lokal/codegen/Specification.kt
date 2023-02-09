package org.github.ferusm.lokal.codegen

data class Specification(
    val groups: List<Group> = emptyList(),
    val metas: Map<String, String> = emptyMap()
) {
    companion object {
        const val DEFAULT_KEY = "default"
        const val META_PREFIX = "\$"
    }

    data class Group(
        val name: String,
        val entries: List<Entry> = emptyList(),
        val metas: Map<String, String> = emptyMap()
    )

    data class Entry(
        val name: String,
        val default: String,
        val translations: Map<String, String> = emptyMap(),
        val metas: Map<String, String> = emptyMap()
    )
}