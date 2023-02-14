package io.github.ferusm.lokal.codegen

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.io.InputStream

object Reader {
    private val mapper = ObjectMapper(YAMLFactory())

    fun read(file: File) = read(file.inputStream())

    fun read(stream: InputStream): Specification {
        val node = mapper.readTree(stream)
        val itemNodeSequence = node.fields().asSequence().filterNotMeta()
        val items = itemNodeSequence.filterNotMeta().map { (name, node) -> readItem(name, node) }.toList()
        val metas = node.parseMetas()
        return Specification(items, metas)
    }

    private fun readItem(name: String, node: JsonNode): Specification.Item = if (node.isEntity()) {
        readEntry(name, node)
    } else {
        val itemNodeSequence = node.fields().asSequence().filterNotMeta()
        val items = itemNodeSequence.filterNotMeta().map { (name, node) -> readItem(name, node) }.toList()
        val meta = node.parseMetas()
        Specification.Group(name, items, meta)
    }

    private fun readEntry(name: String, node: JsonNode): Specification.Entry {
        val translationNodeSequence = node.fields().asSequence().filterNotMeta()
        val translations = translationNodeSequence.onEach { (_, node) ->
            if (!node.isTextual) {
                throw IllegalArgumentException("Every entry must contains only textural values")
            }
        }.filter { (_, node) -> node.isTextual }
            .filter { (name, _) -> name != Specification.DEFAULT_KEY }
            .map { (name, node) ->
                name to node.asText()
            }.toMap()
        val metas = node.parseMetas()
        val defaultNode = node[Specification.DEFAULT_KEY]
            ?: throw IllegalArgumentException("Missing default entry value property ${Specification.DEFAULT_KEY}")
        val defaultValue = if (defaultNode.isTextual) {
            defaultNode.asText()
        } else {
            throw IllegalArgumentException("Default entry value parameter ${Specification.DEFAULT_KEY} must have primitive string value")
        }
        return Specification.Entry(name, defaultValue, translations, metas)
    }
}

private fun Sequence<Map.Entry<String, JsonNode>>.filterNotMeta() = filterNot { (name, _) ->
    name.startsWith(Specification.META_PREFIX)
}

private fun JsonNode.parseMetas(): Map<String, String> {
    val metaNodeSequence = fields().asSequence().filter { (name, _) ->
        name.startsWith(Specification.META_PREFIX)
    }
    return metaNodeSequence.map { (name, node) ->
        if (!node.isTextual) {
            throw IllegalArgumentException("Meta property $name must have primitive string value")
        }
        name.removePrefix(Specification.META_PREFIX) to node.asText()
    }.toMap()
}

private fun JsonNode.isEntity(): Boolean = has(Specification.DEFAULT_KEY)