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

        val rootLocalePropertySpec = PropertySpec.builder("locale", LambdaTypeName.get(returnType = String::class.asTypeName()), KModifier.PUBLIC)
            .initializer("""{ "${Specification.DEFAULT_KEY}" }""")
            .mutable(true)
            .build()
        rootTypeSpec.addProperty(rootLocalePropertySpec)

        val groupTypeSpecs = specification.groups.map { group ->
            val groupTypeClassName = rootClassName.nestedClass(group.name.capitalize())
            TypeSpec.objectBuilder(groupTypeClassName).also { groupTypeSpec ->
                val entryTypeSpecs = group.texts.mapKeys { (key, _) ->
                    groupTypeClassName.nestedClass(key.capitalize())
                }.mapValues { (key, value) ->
                     TypeSpec.classBuilder(key).also { entryTypeSpec ->
                        val entryTypePropertyKeys = value.getTemplateKeys()
                        val entryTypePropertySpecs = entryTypePropertyKeys.map {
                            PropertySpec.builder(it, String::class, KModifier.PRIVATE)
                                .mutable(true)
                                .initializer(""""undefined"""")
                                .build()
                        }
                        entryTypeSpec.addProperties(entryTypePropertySpecs)

                        val entryTypePropertyFunctions = entryTypePropertySpecs.map {
                            FunSpec.builder(it.name).addParameter(it.name, it.type)
                                .returns(key)
                                .addStatement("this.${it.name} = ${it.name}")
                                .addStatement("return this")
                                .build()
                        }
                        entryTypeSpec.addFunctions(entryTypePropertyFunctions)

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
                groupTypeSpec.addTypes(entryTypeSpecs.values)
                val entryTypePropertySpecs = entryTypeSpecs.map { (key, value) ->
                    val getterFunSpec = FunSpec.getterBuilder()
                        .addStatement("""return ${value.name}()""")
                        .build()
                    PropertySpec.builder(key.simpleName.decapitalize(), key, KModifier.PUBLIC)
                        .getter(getterFunSpec)
                        .build()
                }

                groupTypeSpec.addProperties(entryTypePropertySpecs)

            }.build()
        }
        rootTypeSpec.addTypes(groupTypeSpecs)



        return FileSpec.builder(targetPackage, "${NAME}.kt")
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

private fun String.decapitalize() = replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.ENGLISH) else "$it" }

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