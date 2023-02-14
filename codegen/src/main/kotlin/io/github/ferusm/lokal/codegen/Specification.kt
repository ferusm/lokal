package io.github.ferusm.lokal.codegen

data class Specification(
    val items: List<Item> = emptyList(),
    val metas: Map<String, String> = emptyMap()
) {
    companion object {
        const val DEFAULT_KEY = "~"
        const val META_PREFIX = "\$"
    }

    sealed interface Item

    data class Group(
        val name: String,
        val items: List<Item> = emptyList(),
        val metas: Map<String, String> = emptyMap()
    ): Item

    data class Entry(
        val name: String,
        val default: String,
        val translations: Map<String, String> = emptyMap(),
        val metas: Map<String, String> = emptyMap()
    ): Item
}