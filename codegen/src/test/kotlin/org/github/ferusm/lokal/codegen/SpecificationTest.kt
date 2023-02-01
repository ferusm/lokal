package org.github.ferusm.lokal.codegen

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class SpecificationTest {
    @Test
    fun `Simple spec should be successfully deserialized`() {
        val file = this::class.java.classLoader.getResourceAsStream("simple_specification.json")!!
        val actualSpecification: Specification = Json.decodeFromStream(file)
        val expectedSpecification = Specification(
            "SimpleSpecification", mapOf(
                "someMessage" to mapOf("default" to "Hello, comrade", "ru" to "Привет, товарисч")
            )
        )
        assertEquals(expectedSpecification, actualSpecification)
    }
}