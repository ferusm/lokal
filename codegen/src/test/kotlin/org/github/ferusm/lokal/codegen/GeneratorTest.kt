package org.github.ferusm.lokal.codegen

import com.squareup.kotlinpoet.FileSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeneratorTest {
    @Test
    fun `Generator should process empty spec set`() {
        val fileSpec = Generator.generate()
        assertEquals("""
            package ${Generator.PACKAGE}
            
            import kotlin.String

            public object ${Generator.NAME} {
              public var locale: String = "${Generator.DEFAULT}"
            }
            
        """.trimIndent(), fileSpec.asText())
    }

    @Test
    fun `Generator should process single spec without data`() {
        val specification = Specification("http")
        val fileSpec = Generator.generate(specification)
        assertEquals("""
            package ${Generator.PACKAGE}
            
            import kotlin.String

            public object ${Generator.NAME} {
              public var locale: String = "${Generator.DEFAULT}"
            
              public object Http
            }
            
        """.trimIndent(), fileSpec.asText())
    }

    @Test
    fun `Generator should process single spec with single data`() {
        val specification = Specification("http", mapOf(
            "statusMessage" to mapOf("default" to "Hello", "ru" to "Привет")
        ))
        val fileSpec = Generator.generate(specification)
        assertEquals("""
            package ${Generator.PACKAGE}

            import kotlin.String

            public object ${Generator.NAME} {
              public var locale: String = "${Generator.DEFAULT}"
            
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

        """.trimIndent(), fileSpec.asText())
    }

    @Test
    fun `Generator should fail processing of single spec with single data with missed default value`() {
        val specification = Specification("http", mapOf(
            "statusMessage" to mapOf("ru" to "Привет")
        ))
        assertFailsWith<IllegalArgumentException> {
            Generator.generate(specification)
        }
    }

    private fun FileSpec.asText(): String = StringBuilder().also { writeTo(it) }.toString()
}