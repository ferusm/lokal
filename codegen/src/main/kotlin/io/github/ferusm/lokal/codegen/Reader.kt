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
        val groupNodeSequence = node.fields().asSequence().filterNotMeta()
        val groups = groupNodeSequence.map { (name, node) ->
            val entryNodeSequence = node.fields().asSequence().filterNotMeta()
            val entries = entryNodeSequence.map { (name, node) ->
                val translationNodeSequence = node.fields().asSequence().filterNotMeta()
                val translations = translationNodeSequence
                    .filter { (_, node) -> node.isTextual }
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
                Specification.Entry(name, defaultValue, translations, metas)
            }.toList()
            val metas = node.parseMetas()
            Specification.Group(name, entries, metas)
        }.toList()
        val metas = node.parseMetas()
        return Specification(groups, metas)
    }
}

private fun Sequence<Map.Entry<String, JsonNode>>.filterNotMeta() = filterNot { (name, _) ->
    name.startsWith(Specification.META_PREFIX)
}

private fun Sequence<Map.Entry<String, JsonNode>>.filterMeta() = filter { (name, _) ->
    name.startsWith(Specification.META_PREFIX)
}

private fun JsonNode.parseMetas(): Map<String, String> {
    val metaNodeSequence = fields().asSequence().filterMeta()
    return metaNodeSequence.map { (name, node) ->
        if (!node.isTextual) {
            throw IllegalArgumentException("Meta property $name must have primitive string value")
        }
        name.removePrefix(Specification.META_PREFIX) to node.asText()
    }.toMap()
}