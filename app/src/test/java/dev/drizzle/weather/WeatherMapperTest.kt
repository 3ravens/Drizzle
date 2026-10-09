package dev.drizzle.weather

import dev.drizzle.weather.data.mapper.WeatherMapper
import dev.drizzle.weather.data.remote.dto.OpenMeteoResponse
import dev.drizzle.weather.domain.model.RainMode
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class WeatherMapperTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun decode(payload: String): OpenMeteoResponse =
        json.decodeFromString(OpenMeteoResponse.serializer(), payload)

    @Test
    fun `maps current, hourly, daily and minutely rain`() {
        val response = decode(FULL_PAYLOAD)
        val forecast = WeatherMapper.map(response)

        assertEquals(18.5, forecast.current.temperature, 0.001)
        assertEquals(60, forecast.current.humidity)
        assertEquals(true, forecast.current.isDay)

        assertEquals(3, forecast.hourly.size)
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), forecast.hourly.first().time)

        assertEquals(RainMode.MINUTELY, forecast.rain.mode)
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 15), forecast.rain.startsAt)

        assertEquals(20.0, forecast.dailyHigh!!, 0.001)
        assertEquals(11.0, forecast.dailyLow!!, 0.001)
        assertEquals(LocalDateTime.of(2024, 6, 1, 4, 45), forecast.sunrise)
        assertEquals(LocalDateTime.of(2024, 6, 1, 21, 10), forecast.sunset)
    }

    @Test
    fun `falls back to hourly precipitation when minutely is too short`() {
        val response = decode(HOURLY_ONLY_PAYLOAD)
        val forecast = WeatherMapper.map(response)

        assertEquals(RainMode.HOURLY, forecast.rain.mode)
        assertEquals(LocalDateTime.of(2024, 6, 1, 13, 0), forecast.rain.startsAt)
    }

    @Test
    fun `reports no rain when nothing is wet`() {
        val response = decode(DRY_PAYLOAD)
        val forecast = WeatherMapper.map(response)

        assertEquals(RainMode.MINUTELY, forecast.rain.mode)
        assertNull(forecast.rain.startsAt)
    }

    private companion object {
        const val FULL_PAYLOAD = """
        {
          "latitude": 51.5, "longitude": -0.12, "timezone": "Europe/London",
          "current": {"time":"2024-06-01T12:00","temperature_2m":18.5,"apparent_temperature":17.0,"relative_humidity_2m":60,"is_day":1,"precipitation":0.0,"weather_code":1,"wind_speed_10m":12.0},
          "hourly": {
             "time":["2024-06-01T12:00","2024-06-01T13:00","2024-06-01T14:00"],
             "temperature_2m":[18.5,19.0,19.5],
             "precipitation":[0.0,0.2,0.0],
             "precipitation_probability":[10,70,20],
             "weather_code":[1,61,2]
          },
          "minutely_15": {
             "time":["2024-06-01T12:00","2024-06-01T12:15","2024-06-01T12:30","2024-06-01T12:45"],
             "precipitation":[0.0,0.3,0.5,0.4]
          },
          "daily": {
             "time":["2024-06-01","2024-06-02"],
             "weather_code":[1,2],
             "temperature_2m_max":[20.0,21.0],
             "temperature_2m_min":[11.0,12.0],
             "sunrise":["2024-06-01T04:45","2024-06-02T04:45"],
             "sunset":["2024-06-01T21:10","2024-06-02T21:10"]
          }
        }
        """

        const val HOURLY_ONLY_PAYLOAD = """
        {
          "latitude": 51.5, "longitude": -0.12, "timezone": "Europe/London",
          "current": {"time":"2024-06-01T12:00","temperature_2m":18.5,"weather_code":1},
          "hourly": {
             "time":["2024-06-01T12:00","2024-06-01T13:00"],
             "temperature_2m":[18.5,19.0],
             "precipitation":[0.0,0.2],
             "precipitation_probability":[10,70],
             "weather_code":[1,61]
          }
        }
        """

        const val DRY_PAYLOAD = """
        {
          "latitude": 51.5, "longitude": -0.12, "timezone": "Europe/London",
          "current": {"time":"2024-06-01T12:00","temperature_2m":18.5,"weather_code":0},
          "hourly": {
             "time":["2024-06-01T12:00","2024-06-01T13:00"],
             "temperature_2m":[18.5,19.0],
             "precipitation":[0.0,0.0],
             "precipitation_probability":[0,0],
             "weather_code":[0,0]
          },
          "minutely_15": {
             "time":["2024-06-01T12:00","2024-06-01T12:15","2024-06-01T12:30","2024-06-01T12:45"],
             "precipitation":[0.0,0.0,0.0,0.0]
          }
        }
        """
    }
}
