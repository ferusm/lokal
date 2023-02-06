package org.github.ferusm.lokal.codegen

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.github.ferusm.lokal.codegen.Specification.Companion.DEFAULT_KEY
import org.github.ferusm.lokal.codegen.Specification.Companion.DESCRIPTION_KEY
import org.github.ferusm.lokal.codegen.Specification.Companion.META_KEY
import org.github.ferusm.lokal.codegen.Specification.Companion.SUMMARY_KEY
import org.github.ferusm.lokal.codegen.Specification.Companion.VERSION_KEY

object SpecificationSerializer : KSerializer<Specification> {
    private val serializer = MapSerializer(
        String.serializer(),
        MapSerializer(
            String.serializer(),
            MapSerializer(
                String.serializer(),
                String.serializer()
            )
        )
    )

    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun deserialize(decoder: Decoder): Specification {
        val rootMap = serializer.deserialize(decoder)
        val groups = rootMap.map { (name, group) ->
            val entries = (group - META_KEY).mapValues { (name, entry) ->
                val default = requireNotNull(entry[DEFAULT_KEY]) { "$DEFAULT_KEY field is required" }
                val translations = entry - DEFAULT_KEY

                Specification.Entry(name, default, translations)
            }
            val meta = group[META_KEY]
            val version = meta?.get(VERSION_KEY)
            val summary = meta?.get(SUMMARY_KEY)
            val description = meta?.get(DESCRIPTION_KEY)

            Specification.Group(version, summary, description, name, entries)
        }
        return Specification(groups)
    }

    override fun serialize(encoder: Encoder, value: Specification) {
        val rootMap = value.groups.associate { group ->
            val entriesMap = group.texts.mapValues { (_, entry) ->
                mapOf(DEFAULT_KEY to entry.default) + entry.translations
            }
            val metaMap = mapOf(
                VERSION_KEY to (group.version ?: ""),
                SUMMARY_KEY to (group.summary ?: ""),
                DESCRIPTION_KEY to (group.description ?: "")
            ).filterValues(String::isNotEmpty)
            val groupMap = if (metaMap.isNotEmpty()) {
                entriesMap + mapOf(META_KEY to metaMap)
            } else {
                entriesMap
            }
            group.name to groupMap
        }
        serializer.serialize(encoder, rootMap)
    }
}