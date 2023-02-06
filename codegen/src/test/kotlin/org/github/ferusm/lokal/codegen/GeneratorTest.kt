package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.FileSpec
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratorTest {
    @Test
    fun `Generator should process empty spec set`() {
        val fileSpec = Generator.generate()
        assertEquals(
            """
            package ${Generator.PACKAGE}

            public object ${Generator.NAME}

        """.trimIndent(), fileSpec.asText()
        )
    }

    @Test
    fun `Generator should process single spec without data`() {
        val specification = Specification(listOf(Specification.Group(name = "http", texts = emptyMap())))
        val fileSpec = Generator.generate(specification)
        assertEquals(
            """
            package ${Generator.PACKAGE}

            public object ${Generator.NAME} {
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
                        "statusMessage" to Specification.Entry("statusMessage", "Hello", mapOf("ru" to "Привет"))
                    )
                )
            )
        )
        val fileSpec = Generator.generate(specification)
        assertEquals(
            """
            package ${Generator.PACKAGE}

            import kotlin.String

            public object ${Generator.NAME} {
              public object Http {
                public val statusMessage: String
                  get() {
                    when(locale) {
                      "ru" -> "Привет"
                      else -> "Hello"
                    }
                  }
              }
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    private fun FileSpec.asText(): String = StringBuilder().also { writeTo(it) }.toString()
}