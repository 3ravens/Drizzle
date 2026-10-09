package dev.drizzle.weather.domain.model

import java.time.LocalDateTime

/** A location the user can view weather for. */
data class SavedLocation(
    val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
)

data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val isDay: Boolean,
    val precipitation: Double,
    val weatherCode: Int,
    val windSpeed: Double,
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val precipitation: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
)

enum class RainMode { MINUTELY, HOURLY, NONE }

/** A single sample on the rain graph. [value] is mm (minutely) or % (hourly fallback). */
data class RainPoint(val time: LocalDateTime, val value: Float)

data class RainWindow(
    val points: List<RainPoint>,
    val startsAt: LocalDateTime?,
    val mode: RainMode,
)

data class WeatherForecast(
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val rain: RainWindow,
    val dailyHigh: Double?,
    val dailyLow: Double?,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
)

/** A city returned by the Open-Meteo geocoding search. */
data class SearchResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?,
)
