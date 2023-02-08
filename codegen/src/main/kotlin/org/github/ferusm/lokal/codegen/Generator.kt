package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    const val NAME = "LoKal"

    fun generate(targetPackage: String, specification: Specification): FileSpec {
        checkGroupDuplications(specification)
        checkEntryDuplications(specification)

        val rootClassName = ClassName(targetPackage, NAME)
        val rootTypeSpec = TypeSpec.objectBuilder(rootClassName)

        LambdaTypeName.get(returnType = Unit::class.asTypeName())

        val rootLocalePropertySpec = PropertySpec.builder(
            "locale",
            LambdaTypeName.get(returnType = String::class.asTypeName()),
            KModifier.PUBLIC
        )
            .initializer("""{ "${Specification.DEFAULT_KEY}" }""")
            .mutable(true)
            .build()
        rootTypeSpec.addProperty(rootLocalePropertySpec)

        val groupTypeSpecs = specification.groups.map { group ->
            val groupTypeClassName = rootClassName.nestedClass(group.name.capitalize())
            TypeSpec.objectBuilder(groupTypeClassName).also { groupTypeSpec ->
                val entryTypeSpecs = group.texts.mapKeys { (key, _) ->
                    groupTypeClassName.nestedClass(key.capitalize())
                }.map { (key, value) ->
                    TypeSpec.classBuilder(key)
                        .addModifiers(KModifier.DATA)
                        .also { entryTypeSpec ->
                            val entryTypePropertyKeys = value.getTemplateKeys()
                            val constructorSpec = FunSpec.constructorBuilder().also {
                                entryTypePropertyKeys.forEach { propertyKey ->
                                    it.addParameter(propertyKey, String::class)
                                }
                            }.build()
                            entryTypeSpec.primaryConstructor(constructorSpec)

                            val entryTypePropertySpecs = entryTypePropertyKeys.map {
                                PropertySpec.builder(it, String::class, KModifier.PUBLIC)
                                    .initializer(it)
                                    .build()
                            }
                            entryTypeSpec.addProperties(entryTypePropertySpecs)

                            val entryTypeToStringFunction = FunSpec.builder("toString")
                                .returns(String::class)
                                .addModifiers(KModifier.OVERRIDE)
                                .beginControlFlow("return when(${rootClassName.simpleName}.${rootLocalePropertySpec.name}())")
                                .also {
                                    value.translations.forEach { (locale, value) ->
                                        it.addStatement(""""$locale" -> "${value.replace("{", "\${")}"""")
                                    }
                                    it.addStatement("""else -> "${value.default.replace("{", "\${")}"""")
                                }.endControlFlow().build()
                            entryTypeSpec.addFunction(entryTypeToStringFunction)

                        }.build()
                }
                groupTypeSpec.addTypes(entryTypeSpecs)
            }.build()
        }
        rootTypeSpec.addTypes(groupTypeSpecs)



        return FileSpec.builder(targetPackage, NAME)
            .addType(rootTypeSpec.build())
            .build()
    }

    private fun checkGroupDuplications(specification: Specification) {
        val groups = specification.groups.groupBy(Specification.Group::name)
        val duplicatedGroupNames = groups.filterValues { it.size > 1 }.map { it.value.first().name }
        if (duplicatedGroupNames.isNotEmpty()) {
            throw IllegalArgumentException("Duplicated Groups found: '${duplicatedGroupNames.joinToString(", ")}'")
        }
    }

    private fun checkEntryDuplications(specification: Specification) {
        val groups = specification.groups.groupBy(Specification.Group::name)
        val duplicatedEntryNames = groups.mapValues { (_, value) ->
            value.flatMap { it.texts.keys }
        }.mapValues { (_, value) ->
            value.groupBy { it }.mapValues { (_, value) -> value.size }
        }.filterValues {
            it.values.any { it > 1 }
        }.mapValues {
            it.value.keys
        }.flatMap { (key, value) ->
            value.map { "$key/$it" }
        }.joinToString(", ")

        if (duplicatedEntryNames.isNotEmpty()) {
            throw IllegalArgumentException("Duplicated Entries found: '$duplicatedEntryNames'")
        }
    }

}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else "$it" }

private fun Specification.Entry.getTemplateKeys(): Collection<String> = buildSet {
    val keyBuilder: StringBuilder = StringBuilder()
    default.forEach {
        when {
            it == '{' -> {
                keyBuilder.append(' ')
            }

            it == '}' -> {
                add("${keyBuilder.trimStart()}")
                keyBuilder.clear()
            }

            keyBuilder.isNotEmpty() -> {
                if (!it.isLetter()) {
                    throw IllegalArgumentException("Unsupported value of template key of $name entry")
                }
                keyBuilder.append(it)
            }
        }
    }
}