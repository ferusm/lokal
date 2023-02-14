package io.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    private const val NAME = "LoKal"

    fun generate(targetPackage: String, specification: Specification): FileSpec {
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

        val types = specification.items.map(::processItem).checkDuplications(rootTypeSpec)

        rootTypeSpec.addTypes(types)
        rootTypeSpec.addKdoc(specification.metas.toDocCodeBlock())

        return FileSpec.builder(targetPackage, NAME)
            .addType(rootTypeSpec.build())
            .build()
    }

    private fun processItem(item: Specification.Item): TypeSpec = when (item) {
        is Specification.Entry -> processEntry(item)
        is Specification.Group -> processGroup(item)
    }

    private fun processGroup(group: Specification.Group): TypeSpec {
        val specBuilder = TypeSpec.objectBuilder(group.name.capitalize())

        val types = group.items.map(::processItem).checkDuplications(specBuilder)

        specBuilder.addTypes(types)
        specBuilder.addKdoc(group.metas.toDocCodeBlock())

        return specBuilder.build()
    }

    private fun List<TypeSpec>.checkDuplications(typeSpecBuilder: TypeSpec.Builder): List<TypeSpec> {
        return onEach {  child ->
            if (typeSpecBuilder.typeSpecs.any { it.name == child.name}) {
                throw IllegalArgumentException("Illegal item duplicate ${child.name}")
            }
            if (count { it.name == child.name } > 1) {
                throw IllegalArgumentException("Illegal item duplicate ${child.name}")
            }
        }
    }

    private fun processEntry(entry: Specification.Entry): TypeSpec {
        val specBuilder = TypeSpec.classBuilder(entry.name.capitalize())

        val defaultTemplateKeys = entry.default.getTemplateKeys()
        if (defaultTemplateKeys.isNotEmpty()) {
            val constructorSpec = FunSpec.constructorBuilder().also {
                defaultTemplateKeys.forEach { propertyKey ->
                    it.addParameter(propertyKey, Any::class)
                }
            }.build()
            specBuilder.primaryConstructor(constructorSpec)

            val entryTypePropertySpecs = defaultTemplateKeys.map {
                PropertySpec.builder(it, Any::class, KModifier.PUBLIC)
                    .initializer(it)
                    .build()
            }
            specBuilder.addProperties(entryTypePropertySpecs)
        }

        val entryValueParameterSpec = FunSpec.builder("render")
            .addModifiers(KModifier.PUBLIC)
            .returns(String::class)
            .beginControlFlow("return when($NAME.locale())")
            .apply {
                entry.translations.onEach { (name, value) ->
                    val templateKeys = value.getTemplateKeys()
                    if (templateKeys != defaultTemplateKeys) {
                        throw IllegalArgumentException("Unexpected template keys in $name entry. Template keys in each translation must be the same as in default translation")
                    }
                }.forEach { (locale, value) ->
                    addStatement(""""$locale" -> "${value.replace("{", "\${")}"""")
                }
                addStatement("""else -> "${entry.default.replace("{", "\${")}"""")
            }.endControlFlow().build()
        specBuilder.addFunction(entryValueParameterSpec)

        val entryTypeToStringFunction = FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return ${entryValueParameterSpec.name}()")
            .build()

        specBuilder.addFunction(entryTypeToStringFunction)
        specBuilder.addKdoc(entry.metas.toDocCodeBlock())

        if (defaultTemplateKeys.isNotEmpty()) {
            specBuilder.addModifiers(KModifier.DATA)
        }

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