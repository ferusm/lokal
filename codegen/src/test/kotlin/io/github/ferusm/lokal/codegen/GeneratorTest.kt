package io.github.ferusm.lokal.codegen

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
              public var locale: () -> String = { "~" }
            }

        """.trimIndent(), fileSpec.asText()
        )
    }

    @Test
    fun `Generator should process single spec without data`() {
        val specification = Specification(listOf(Specification.Group("http", emptyList())))
        val fileSpec = Generator.generate("org.test.test.test", specification)
        assertEquals(
            """
            package org.test.test.test

            import kotlin.String

            public object LoKal {
              public var locale: () -> String = { "~" }

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
                    "http", listOf(
                        Specification.Entry(
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}"),
                            mapOf("summary" to "Entry summary")
                        )
                    ),
                    mapOf("summary" to "Group summary")
                )
            ),
            mapOf("summary" to "Root summary")
        )
        val fileSpec = Generator.generate("org.test.test", specification)
        assertEquals(
            """
            package org.test.test
            
            import kotlin.Any
            import kotlin.String
            
            /**
             * Summary - Root summary
             */
            public object LoKal {
              public var locale: () -> String = { "~" }
            
              /**
               * Summary - Group summary
               */
              public object Http {
                /**
                 * Summary - Entry summary
                 */
                public data class StatusMessage(
                  public val comrade: Any,
                ) {
                  public fun render(): String = when(locale()) {
                    "ru" -> ""${'"'}Привет, ${"$"}{comrade}""${'"'}
                    else -> ""${'"'}Hello, ${"$"}{comrade}""${'"'}
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
                    "http", listOf(
                        Specification.Entry(
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                ),
                Specification.Group(
                    "http", listOf(
                        Specification.Entry(
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
                    "http", listOf(
                        Specification.Entry(
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        ),
                        Specification.Entry(
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {comrade}")
                        )
                    )
                ),
                Specification.Group(
                    "rpc", listOf(
                        Specification.Entry(
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
        val specification = Specification(
            listOf(
                Specification.Group(
                    "http", listOf(
                        Specification.Entry(
                            "statusMessage",
                            "Hello, {comrade}",
                            mapOf("ru" to "Привет, {name}")
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
    fun `Generator should product data class if translation has at least one template key`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    "http", listOf(
                        Specification.Entry(
                            "statusMessage",
                            "Hello, comrade"
                        )
                    )
                )
            )
        )
        val fileSpec = Generator.generate("org.test.test", specification)
        assertEquals("""
        package org.test.test
        
        import kotlin.String
        
        public object LoKal {
          public var locale: () -> String = { "~" }
        
          public object Http {
            public class StatusMessage {
              public fun render(): String = when(locale()) {
                else -> ""${'"'}Hello, comrade""${'"'}
              }
        
              public override fun toString(): String = render()
            }
          }
        }

        """.trimIndent(), fileSpec.asText())
    }

    @Test
    fun `Generator should produce multi-level object hierarchy`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    "http", listOf(
                        Specification.Entry(
                            "groupMessage",
                            "Hello from group message"
                        ),
                        Specification.Group(
                            "error", listOf(
                                Specification.Entry(
                                    "groupInGroupMessage",
                                    "Hello from group in group message"
                                )
                            )
                        )
                    )
                ),
                Specification.Entry(
                    "rootMessage",
                    "Hello from root message"
                )
            )
        )
        val fileSpec = Generator.generate("org.test.test", specification)
        assertEquals("""
        package org.test.test

        import kotlin.String
        
        public object LoKal {
          public var locale: () -> String = { "~" }
        
          public object Http {
            public class GroupMessage {
              public fun render(): String = when(locale()) {
                else -> ""${'"'}Hello from group message""${'"'}
              }
        
              public override fun toString(): String = render()
            }
        
            public object Error {
              public class GroupInGroupMessage {
                public fun render(): String = when(locale()) {
                  else -> ""${'"'}Hello from group in group message""${'"'}
                }
        
                public override fun toString(): String = render()
              }
            }
          }
        
          public class RootMessage {
            public fun render(): String = when(locale()) {
              else -> ""${'"'}Hello from root message""${'"'}
            }
        
            public override fun toString(): String = render()
          }
        }

        """.trimIndent(), fileSpec.asText())
    }

    @Test
    fun `Generator should render large string into valid code`() {
        val specification = Specification(
            listOf(
                Specification.Group(
                    "http", listOf(
                        Specification.Entry(
                            "groupMessage",
                            "Hello from group message. Hello from group message. Hello from group message. Hello from group message. Hello from group message. Hello from group message"
                        )
                    )
                )
            )
        )

        val fileSpec = Generator.generate("org.test.test", specification)
        assertEquals("""
        package org.test.test
        
        import kotlin.String
        
        public object LoKal {
          public var locale: () -> String = { "~" }
        
          public object Http {
            public class GroupMessage {
              public fun render(): String = when(locale()) {
                else ->
                    ""${'"'}Hello from group message. Hello from group message. Hello from group message. Hello from group message. Hello from group message. Hello from group message""${'"'}
              }
        
              public override fun toString(): String = render()
            }
          }
        }

        """.trimIndent(), fileSpec.asText())
    }

    private fun FileSpec.asText(): String = StringBuilder().also { writeTo(it) }.toString()
}