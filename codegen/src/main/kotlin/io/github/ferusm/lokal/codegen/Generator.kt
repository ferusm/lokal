package io.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    private const val NAME = "LoKal"

    @Suppress("UNCHECKED_CAST")
    fun generate(targetPackage: String, specification: Specification): FileSpec {
        val rootClassName = ClassName(targetPackage, NAME)
        val specBuilder = TypeSpec.objectBuilder(rootClassName)

        LambdaTypeName.get(returnType = Unit::class.asTypeName())

        val rootLocalePropertySpec = PropertySpec.builder(
            "locale",
            LambdaTypeName.get(returnType = String::class.asTypeName()),
            KModifier.PUBLIC
        ).initializer("{ %S }", Specification.DEFAULT_KEY)
            .mutable(true)
            .build()
        specBuilder.addProperty(rootLocalePropertySpec)

        val definitions = specification.items.map { processItem(rootLocalePropertySpec, it) }.groupBy { it::class }

        val objectDefinition = (definitions[TypeSpec::class] as List<TypeSpec>?)
            ?.checkTypeDuplications(specBuilder) ?: emptyList()
        specBuilder.addTypes(objectDefinition)

        val funDefinitions = (definitions[FunSpec::class] as List<FunSpec>?)
            ?.checkFunDuplications(specBuilder) ?: emptyList()
        specBuilder.addFunctions(funDefinitions)

        specBuilder.addKdoc(specification.metas.toDocCodeBlock())

        return FileSpec.builder(targetPackage, NAME)
            .addType(specBuilder.build())
            .build()
    }

    private fun processItem(localeSpec: PropertySpec, item: Specification.Item): Any = when (item) {
        is Specification.Entry -> processEntry(localeSpec, item)
        is Specification.Group -> processGroup(localeSpec, item)
    }

    @Suppress("UNCHECKED_CAST")
    private fun processGroup(localeSpec: PropertySpec, group: Specification.Group): TypeSpec {
        val specBuilder = TypeSpec.objectBuilder(group.name.capitalize())

        val definitions = group.items.map { processItem(localeSpec, it) }.groupBy { it::class }

        val objectDefinition = (definitions[TypeSpec::class] as List<TypeSpec>?)
            ?.checkTypeDuplications(specBuilder) ?: emptyList()
        specBuilder.addTypes(objectDefinition)

        val funDefinitions = (definitions[FunSpec::class] as List<FunSpec>?)
            ?.checkFunDuplications(specBuilder) ?: emptyList()
        specBuilder.addFunctions(funDefinitions)

        specBuilder.addKdoc(group.metas.toDocCodeBlock())

        return specBuilder.build()
    }

    private fun List<TypeSpec>.checkTypeDuplications(specBuilder: TypeSpec.Builder): List<TypeSpec> {
        return onEach { child ->
            if (specBuilder.typeSpecs.any { it.name == child.name }) {
                throw IllegalArgumentException("Illegal group duplicate ${child.name}")
            }
            if (count { it.name == child.name } > 1) {
                throw IllegalArgumentException("Illegal group duplicate ${child.name}")
            }
        }
    }

    private fun List<FunSpec>.checkFunDuplications(specBuilder: TypeSpec.Builder): List<FunSpec> {
        return onEach { child ->
            if (specBuilder.funSpecs.any { it.name == child.name }) {
                throw IllegalArgumentException("Illegal entry duplicate ${child.name}")
            }
            if (count { it.name == child.name } > 1) {
                throw IllegalArgumentException("Illegal entry duplicate ${child.name}")
            }
        }
    }

    private fun processEntry(localeSpec: PropertySpec, entry: Specification.Entry): FunSpec {
        val specBuilder = FunSpec.builder(entry.name)
            .addModifiers(KModifier.PUBLIC)
            .returns(String::class)

        val defaultTemplateKeys = entry.default.getTemplateKeys()
        if (defaultTemplateKeys.isNotEmpty()) {
            defaultTemplateKeys.forEach { templateKey ->
                specBuilder.addParameter(templateKey, Any::class)
            }
        }

        specBuilder.beginControlFlow("return when(%N())", localeSpec)
        entry.translations.onEach { (name, value) ->
            val templateKeys = value.getTemplateKeys()
            if (templateKeys != defaultTemplateKeys) {
                throw IllegalArgumentException("Unexpected template keys in $name entry. Template keys in each translation must be the same as in default translation")
            }
        }.forEach { (locale, value) ->
            specBuilder.addStatement("%S -> %P", locale, value.replace("{", "\${"))
        }
        specBuilder.addStatement("else -> %P", entry.default.replace("{", "\${"))
        specBuilder.endControlFlow()

        specBuilder.addKdoc(entry.metas.toDocCodeBlock())

        return specBuilder.build()
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