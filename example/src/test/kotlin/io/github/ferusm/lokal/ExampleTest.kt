package io.github.ferusm.lokal


import local.schema.test.generating.LoKal
import kotlin.test.Test
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun `All phrases should be generated as classes`() {
        assertEquals("Hello, first comrade Petr", "${LoKal.FirstGroup.FirstMessage("Petr")}")
        assertEquals("Hello, second comrade Ivan", "${LoKal.SecondGroup.SomeMessage("Ivan")}")
    }

    @Test
    fun `Locale parameter should control language`() {
        assertEquals("Hello, first comrade Petr", "${LoKal.FirstGroup.FirstMessage("Petr")}")
        assertEquals("Hello, second comrade Ivan", "${LoKal.SecondGroup.SomeMessage("Ivan")}")

        LoKal.locale = { "ru" }

        assertEquals("Привет, первый товарисч Petr", "${LoKal.FirstGroup.FirstMessage("Petr")}")
        assertEquals("Привет, второй товарисч Ivan", "${LoKal.SecondGroup.SomeMessage("Ivan")}")
    }

    @Test
    fun `Default locale should be used if entry don't have proper translation`() {
        assertEquals("Привет, первый товарисч Petr", "${LoKal.FirstGroup.FirstMessage("Petr")}")
        assertEquals("Привет, второй товарисч Ivan", "${LoKal.SecondGroup.SomeMessage("Ivan")}")

        LoKal.locale = { "jp" }

        assertEquals("Hello, first comrade Petr", "${LoKal.FirstGroup.FirstMessage("Petr")}")
        assertEquals("Hello, second comrade Ivan", "${LoKal.SecondGroup.SomeMessage("Ivan")}")
        LoKal
    }

    @Test
    fun `Render method should return same as toString method return`() {
        assertEquals(LoKal.FirstGroup.FirstMessage("Petr").render(), "${LoKal.FirstGroup.FirstMessage("Petr")}")
    }
}