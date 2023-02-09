package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.FileSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class GeneratorTest {
    @Test
    fun `Generator should process empty spec set`() {
        val fileSpec = Generator.generate("org.test.test", Specification(emptyList()))
        assertEquals(
            """
            package org.test.test

            import kotlin.String

            public object LoKal {
              public var locale: () -> String = { "default" }
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    @Test
    fun `Generator should process single spec without data`() {
        val specification = Specification(listOf(Specification.Group(name = "http", entries = emptyList())))
        val fileSpec = Generator.generate("org.test.test.test", specification)
        assertEquals(
            """
            package org.test.test.test

            import kotlin.String
            
            public object LoKal {
              public var locale: () -> String = { "default" }

              public object Http
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    @Test
    fun `Generator should process single spec with single data`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    name = "http", entries = listOf(
                        Specification.Entry(
                            null,
                            null,
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                )
            )
        )
        val fileSpec = Generator.generate("org.test.test", specification)
        assertEquals(
            """
            package org.test.test
            
            import kotlin.String
            
            public object LoKal {
              public var locale: () -> String = { "default" }
            
              public object Http {
                public data class StatusMessage(
                  public val comrade: String,
                ) {
                  public fun render(): String = when(LoKal.locale()) {
                    "ru" -> "Привет, ${"$"}{comrade}"
                    else -> "Hello, ${"$"}{comrade}"
                  }
            
                  public override fun toString(): String = render()
                }
              }
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    @Test
    fun `Generator should throw an exception if groups was duplicated`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    name = "http", entries = listOf(
                        Specification.Entry(
                            null,
                            null,
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                ),
                Specification.Group(
                    name = "http", entries = listOf(
                        Specification.Entry(
                            null,
                            null,
                            "anotherMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                )
            )
        )
        assertFails {
            Generator.generate("org.test.test", specification)
        }
    }

    @Test
    fun `Generator should throw an exception if entries was duplicated`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    name = "http", entries = listOf(
                        Specification.Entry(
                            null,
                            null,
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        ),
                        Specification.Entry(
                            null,
                            null,
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                ),
                Specification.Group(
                    name = "rpc", entries = listOf(
                        Specification.Entry(
                            null,
                            null,
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                )
            )
        )
        assertFails {
            Generator.generate("org.test.test", specification)
        }
    }

    @Test
    fun `Generator should throw an exception if translation has not the same template keys as default translation has`() {

    }

    private fun FileSpec.asText(): String = StringBuilder().also { writeTo(it) }.toString()
}