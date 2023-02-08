package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.FileSpec
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val specification = Specification(listOf(Specification.Group(name = "http", texts = emptyMap())))
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
                    name = "http", texts = mapOf(
                        "statusMessage" to Specification.Entry(
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
                  public override fun toString(): String = when(LoKal.locale()) {
                    "ru" -> "Привет, ${"$"}{comrade}"
                    else -> "Hello, ${"$"}{comrade}"
                  }
                }
              }
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    private fun FileSpec.asText(): String = StringBuilder().also { writeTo(it) }.toString()
}