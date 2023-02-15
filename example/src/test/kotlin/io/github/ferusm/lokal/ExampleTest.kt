package io.github.ferusm.lokal


import local.schema.test.generating.LoKal
import kotlin.test.Test
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun `All phrases should be generated as inline functions`() {
        assertEquals("Hello, first comrade Petr", LoKal.FirstGroup.firstMessage("Petr"))
        assertEquals("Hello, second comrade Ivan", LoKal.SecondGroup.someMessage("Ivan"))
    }

    @Test
    fun `Locale parameter should control language`() {
        assertEquals("Hello, first comrade Petr", LoKal.FirstGroup.firstMessage("Petr"))
        assertEquals("Hello, second comrade Ivan", LoKal.SecondGroup.someMessage("Ivan"))

        LoKal.locale = { "ru" }

        assertEquals("Привет, первый товарисч Petr", LoKal.FirstGroup.firstMessage("Petr"))
        assertEquals("Привет, второй товарисч Ivan", LoKal.SecondGroup.someMessage("Ivan"))
    }

    @Test
    fun `Default locale should be used if entry don't have proper translation`() {
        assertEquals("Привет, первый товарисч Petr", LoKal.FirstGroup.firstMessage("Petr"))
        assertEquals("Привет, второй товарисч Ivan", LoKal.SecondGroup.someMessage("Ivan"))

        LoKal.locale = { "jp" }

        assertEquals("Hello, first comrade Petr", LoKal.FirstGroup.firstMessage("Petr"))
        assertEquals("Hello, second comrade Ivan", LoKal.SecondGroup.someMessage("Ivan"))
    }


    @Test
    fun `Template constructor should apply Kotlin Any parameters`() {
        assertEquals("Hello, second comrade 1", LoKal.SecondGroup.someMessage(1))
    }
}