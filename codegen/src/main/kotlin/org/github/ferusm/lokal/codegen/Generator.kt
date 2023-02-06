package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    const val PACKAGE = "org.github.ferusm.lokal"
    const val NAME = "Lokal"

    fun generate(vararg specifications: Specification): FileSpec {
        val groups = specifications.flatMap(Specification::groups)

        val duplicatedNames = groups.getDuplicatedNames()
        if (duplicatedNames.isNotEmpty()) {
            throw IllegalArgumentException("Group name must be unique in scope off all specifications. Duplicated names: ${duplicatedNames.joinToString(", ")}")
        }

        val subTypes = groups.map { group ->
            val name = group.name.capitalize()
            val className = ClassName(PACKAGE, name)
            TypeSpec.objectBuilder(className).also { type ->
                val properties = group.texts.map { (name, locales) ->
                    val getter = FunSpec.getterBuilder().beginControlFlow("when(locale)").also { function ->
                        locales.translations.forEach { (locale, value) ->
                            function.addStatement(""""$locale" -> "$value"""")
                        }
                        function.addStatement("""else -> "${locales.default}"""")
                    }.endControlFlow().build()
                    PropertySpec.builder(name, String::class).getter(getter).build()
                }
                type.addProperties(properties)
            }.build()
        }
        val className = ClassName(PACKAGE, NAME)
        val rootType = TypeSpec.objectBuilder(className).apply { addTypes(subTypes) }.build()
        return FileSpec.builder(PACKAGE, "${NAME}.kt")
            .addType(rootType)
            .build()
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else "$it" }

    private fun List<Specification.Group>.getDuplicatedNames(): Collection<String> {
        val groupNames = map(Specification.Group::name)
        return groupNames - groupNames.toSet()
    }
}