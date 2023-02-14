package io.github.ferusm.lokal.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderTest {
    @Test
    fun `Reader should parse specification file`() {
        val stream = this::class.java.classLoader.getResourceAsStream("simple_specification.yaml")!!
        val actualSpecification = Reader.read(stream)
        val expectedSpecification = Specification(
            listOf(
                Specification.Entry(
                    "message",
                    "Hello, comrade {name}",
                    mapOf("ru" to "Привет, товарисч {name}"),
                    mapOf(
                        "summary" to "Basic message",
                        "description" to "Basic message for test porpoises"
                    )
                ),
                Specification.Group(
                    "http",
                    listOf(
                        Specification.Entry(
                            "message",
                            "Hello, comrade {name}",
                            mapOf("ru" to "Привет, товарисч {name}"),
                            mapOf(
                                "summary" to "Basic message",
                                "description" to "Basic message for test porpoises"
                            )
                        ), Specification.Group(
                            "http",
                            listOf(
                                Specification.Entry(
                                    "message",
                                    "Hello, comrade {name}",
                                    mapOf("ru" to "Привет, товарисч {name}"),
                                    mapOf(
                                        "summary" to "Basic message",
                                        "description" to "Basic message for test porpoises"
                                    )
                                )
                            ),
                            mapOf(
                                "version" to "0.5.0",
                                "summary" to "Http text entries",
                                "description" to "Http status messages and etc."
                            )
                        )
                    ),
                    mapOf(
                        "version" to "0.5.0",
                        "summary" to "Http text entries",
                        "description" to "Http status messages and etc."
                    )
                )
            ),
            mapOf(
                "version" to "0.5.0",
                "summary" to "Text entries",
                "description" to "Status messages and etc."
            )
        )
        assertEquals(expectedSpecification, actualSpecification)
    }
}