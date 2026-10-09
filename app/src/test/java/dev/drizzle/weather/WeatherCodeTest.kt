package dev.drizzle.weather

import dev.drizzle.weather.util.WeatherCode
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeTest {

    @Test
    fun `maps known codes to descriptions`() {
        assertEquals("Clear sky", WeatherCode.description(0))
        assertEquals("Rain", WeatherCode.description(63))
        assertEquals("Thunderstorm", WeatherCode.description(95))
        assertEquals("Snow", WeatherCode.description(73))
    }

    @Test
    fun `unknown codes are labelled`() {
        assertEquals("Unknown", WeatherCode.description(1234))
    }
}
