package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    private const val NAME = "LoKal"

    fun generate(targetPackage: String, specification: Specification): FileSpec {
        checkGroupDuplications(specification)
        checkEntryDuplications(specification)
        checkEntryTemplateKeyConsistent(specification)

        val rootClassName = ClassName(targetPackage, NAME)
        val rootTypeSpec = TypeSpec.objectBuilder(rootClassName)

        LambdaTypeName.get(returnType = Unit::class.asTypeName())

        val rootLocalePropertySpec = PropertySpec.builder(
            "locale",
            LambdaTypeName.get(returnType = String::class.asTypeName()),
            KModifier.PUBLIC
        ).initializer("""{ "${Specification.DEFAULT_KEY}" }""")
            .mutable(true)
            .build()
        rootTypeSpec.addProperty(rootLocalePropertySpec)

        val groupTypeSpecs = specification.groups.map { group ->
            val groupTypeClassName = rootClassName.nestedClass(group.name.capitalize())
            TypeSpec.objectBuilder(groupTypeClassName).also { groupTypeSpec ->
                val entryTypeSpecs = group.entries.map { entry ->
                    entry to groupTypeClassName.nestedClass(entry.name.capitalize())
                }.map { (entry, className) ->
                    TypeSpec.classBuilder(className)
                        .addModifiers(KModifier.DATA)
                        .also { entryTypeSpec ->
                            val entryTypePropertyKeys = entry.default.getTemplateKeys()
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

                            val entryValueParameterSpec = FunSpec.builder("render")
                                .addModifiers(KModifier.PUBLIC)
                                .returns(String::class)
                                .beginControlFlow("return when(${rootClassName.simpleName}.${rootLocalePropertySpec.name}())")
                                .apply {
                                    entry.translations.forEach { (locale, value) ->
                                        addStatement(""""$locale" -> "${value.replace("{", "\${")}"""")
                                    }
                                    addStatement("""else -> "${entry.default.replace("{", "\${")}"""")
                                }.endControlFlow().build()
                            entryTypeSpec.addFunction(entryValueParameterSpec)

                            val entryTypeToStringFunction = FunSpec.builder("toString")
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(String::class)
                                .addStatement("return ${entryValueParameterSpec.name}()")
                                .build()
                            entryTypeSpec.addFunction(entryTypeToStringFunction)

                            entryTypeSpec.addKdoc(entry.metas.toDocCodeBlock())
                        }.build()
                }
                groupTypeSpec.addTypes(entryTypeSpecs)
                groupTypeSpec.addKdoc(group.metas.toDocCodeBlock())
            }.build()
        }
        rootTypeSpec.addTypes(groupTypeSpecs)
        rootTypeSpec.addKdoc(specification.metas.toDocCodeBlock())

        return FileSpec.builder(targetPackage, NAME)
            .addType(rootTypeSpec.build())
            .build()
    }

    private fun checkGroupDuplications(specification: Specification) {
        val groups = specification.groups.groupBy(Specification.Group::name)
        val duplicatedGroupNames = groups.filterValues { it.size > 1 }.map { it.value.first().name }
        if (duplicatedGroupNames.isNotEmpty()) {
            val description = duplicatedGroupNames.joinToString(", ")
            throw IllegalArgumentException("Every group should have unique name in scope of specification. Invalid groups is: $description")
        }
    }

    private fun checkEntryDuplications(specification: Specification) {
        val groupedEntryNames = specification.groups.associateWith(Specification.Group::entries)
        val invalidGroupedEntryList = groupedEntryNames.mapValues { (_, entryList) ->
            val entryNameList = entryList.map(Specification.Entry::name)
            entryNameList.filter { name -> entryNameList.count { it == name } > 1 }
        }.filterValues { diff -> diff.isNotEmpty() }
        if (invalidGroupedEntryList.isNotEmpty()) {
            val description = invalidGroupedEntryList.flatMap { (group, entryNameList) ->
                entryNameList.map { entryName -> "${group.name}/$entryName" }
            }.joinToString(", ")
            throw IllegalArgumentException("Every entry should have unique name in scope of group. Invalid entries is: $description")
        }
    }

    private fun checkEntryTemplateKeyConsistent(specification: Specification) {
        val groupedEntries = specification.groups.associateWith(Specification.Group::entries)
        val invalidGroupedEntries = groupedEntries.mapValues { (_, entryList) ->
            entryList.filter { entry ->
                val defaultKeys = entry.default.getTemplateKeys()
                entry.translations.any { (_, value) ->
                    val keys = value.getTemplateKeys()
                    !defaultKeys.containsAll(keys)
                }
            }
        }.filterValues { it.isNotEmpty() }
        if (invalidGroupedEntries.isNotEmpty()) {
            val description = invalidGroupedEntries.flatMap { (group, entryList) ->
                entryList.map { entry -> "${group.name}/${entry.name}" }
            }.joinToString(", ")
            throw IllegalArgumentException("Every translation should have same template keys as is default translation use. Invalid entries is: $description")
        }
    }

}

private fun Map<String, String>.toDocCodeBlock(): CodeBlock = CodeBlock.builder().apply {
    forEach { (key, value) ->
        addStatement("${key.capitalize()} - $value")
    }
}.build()

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else "$it" }

private fun String.getTemplateKeys(): Set<String> {
    val result = mutableSetOf<String>()
    val keyBuilder: StringBuilder = StringBuilder()
    forEach {
        when {
            it == '{' -> {
                keyBuilder.append(' ')
            }

            it == '}' -> {
                result.add("${keyBuilder.trimStart()}")
                keyBuilder.clear()
            }

            keyBuilder.isNotEmpty() -> {
                if (!it.isLetter()) {
                    throw IllegalArgumentException("Unsupported value of template key of $this entry")
                }
                keyBuilder.append(it)
            }
        }
    }
    return result.toSet()
}