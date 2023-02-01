package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.*
import java.util.*

object Generator {
    const val PACKAGE = "org.github.ferusm.lokal"
    const val NAME = "Lokal"
    const val DEFAULT = "default"

    fun generate(vararg specifications: Specification): FileSpec {
        val childObjects = specifications.map { spec ->
            val name = spec.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else "$it" }
            TypeSpec.objectBuilder(ClassName(PACKAGE, name)).also { type ->
                val entryProperties = spec.data.map { (name, locales) ->
                    val defaultValue = locales[DEFAULT] ?: throw IllegalArgumentException("Unspecified 'default' value")
                    val getter = FunSpec.getterBuilder()
                        .beginControlFlow("when(locale)").apply {
                            locales.filter { (locale, _) -> locale != DEFAULT }.forEach { (locale, value) ->
                                addStatement(""""$locale" -> "$value"""")
                            }
                            addStatement("""else -> "$defaultValue"""")
                        }.endControlFlow()
                        .build()
                    PropertySpec.builder(name, String::class).getter(getter).build()
                }
                type.addProperties(entryProperties)
            }.build()
        }
        val parentObject = TypeSpec.objectBuilder(ClassName(PACKAGE, NAME)).apply {
            val localeProperty = PropertySpec.builder("locale", String::class)
                .mutable(true)
                .initializer(""""$DEFAULT"""")
                .build()
            addProperty(localeProperty)
            addTypes(childObjects)
        }.build()
        return FileSpec.builder(PACKAGE, "${NAME}.kt")
            .addType(parentObject)
            .build()
    }
}