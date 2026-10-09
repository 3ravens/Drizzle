package dev.drizzle.weather.data.mapper

import dev.drizzle.weather.data.remote.dto.OpenMeteoResponse
import dev.drizzle.weather.domain.model.CurrentWeather
import dev.drizzle.weather.domain.model.HourlyForecast
import dev.drizzle.weather.domain.model.RainMode
import dev.drizzle.weather.domain.model.RainPoint
import dev.drizzle.weather.domain.model.RainWindow
import dev.drizzle.weather.domain.model.WeatherForecast
import java.time.LocalDateTime

/**
 * Maps the Open-Meteo payload into domain models.
 *
 * All timestamps returned by Open-Meteo come back as *local* ISO strings
 * (we always request `timezone=auto`), so they are parsed without any
 * timezone conversion and compared against the location-local `current.time`.
 */
object WeatherMapper {

    private const val MINUTELY_MAX_POINTS = 24 // 6h * 4
    private const val HOURLY_MAX_POINTS = 6
    private const val MIN_MINUTELY_POINTS = 4 // need at least an hour of 15-min data
    private const val RAIN_THRESHOLD_MM = 0.1f

    fun map(response: OpenMeteoResponse): WeatherForecast {
        val currentDto = response.current
        val reference: LocalDateTime = currentDto?.time
            ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            ?: LocalDateTime.now()

        val current = CurrentWeather(
            time = reference,
            temperature = currentDto?.temperature ?: 0.0,
            apparentTemperature = currentDto?.apparentTemperature ?: currentDto?.temperature ?: 0.0,
            humidity = currentDto?.humidity ?: 0,
            isDay = (currentDto?.isDay ?: 1) == 1,
            precipitation = currentDto?.precipitation ?: 0.0,
            weatherCode = currentDto?.weatherCode ?: 0,
            windSpeed = currentDto?.windSpeed ?: 0.0,
        )

        val hourly = buildHourly(response, reference)
        val rain = buildRain(response, reference)
        val dayIndex = response.daily?.time?.indexOf(reference.toLocalDate().toString()) ?: -1

        return WeatherForecast(
            timezone = response.timezone.orEmpty(),
            current = current,
            hourly = hourly,
            rain = rain,
            dailyHigh = response.daily?.temperatureMax?.getOrNull(dayIndex),
            dailyLow = response.daily?.temperatureMin?.getOrNull(dayIndex),
            sunrise = response.daily?.sunrise?.getOrNull(dayIndex)?.toLocalDateTimeOrNull(),
            sunset = response.daily?.sunset?.getOrNull(dayIndex)?.toLocalDateTimeOrNull(),
        )
    }

    private fun buildHourly(response: OpenMeteoResponse, reference: LocalDateTime): List<HourlyForecast> {
        val h = response.hourly ?: return emptyList()
        val start = reference.withMinute(0).withSecond(0).withNano(0)
        val result = ArrayList<HourlyForecast>()
        for (i in h.time.indices) {
            val time = h.time[i].toLocalDateTimeOrNull() ?: continue
            if (time.isBefore(start)) continue
            result += HourlyForecast(
                time = time,
                temperature = h.temperature.getOrNull(i) ?: 0.0,
                precipitation = h.precipitation.getOrNull(i) ?: 0.0,
                precipitationProbability = h.precipitationProbability.getOrNull(i) ?: 0,
                weatherCode = h.weatherCode.getOrNull(i) ?: 0,
            )
            if (result.size >= 24) break
        }
        return result
    }

    private fun buildRain(response: OpenMeteoResponse, reference: LocalDateTime): RainWindow {
        val minutely = response.minutely15
        if (minutely != null) {
            val points = collectPoints(
                times = minutely.time,
                values = minutely.precipitation.map { it.toFloat() },
                reference = reference,
                maxPoints = MINUTELY_MAX_POINTS,
            )
            // Some regions return only the current instant for minutely_15 — fall back if too short.
            if (points.size >= MIN_MINUTELY_POINTS) {
                val start = points.firstOrNull { it.value >= RAIN_THRESHOLD_MM }?.time
                return RainWindow(points, start, RainMode.MINUTELY)
            }
        }

        val hourly = response.hourly
        if (hourly != null) {
            val points = collectPoints(
                times = hourly.time,
                values = hourly.precipitation.map { it.toFloat() },
                reference = reference,
                maxPoints = HOURLY_MAX_POINTS,
            )
            if (points.isNotEmpty()) {
                val start = points.firstOrNull { it.value >= RAIN_THRESHOLD_MM }?.time
                return RainWindow(points, start, RainMode.HOURLY)
            }
        }

        return RainWindow(emptyList(), null, RainMode.NONE)
    }

    /**
     * Takes samples from roughly [reference] onward, up to [maxPoints], padding
     * the reference forward to the nearest sample boundary handled naturally by
     * the `>= reference` filter.
     */
    private fun collectPoints(
        times: List<String>,
        values: List<Float>,
        reference: LocalDateTime,
        maxPoints: Int,
    ): List<RainPoint> {
        val result = ArrayList<RainPoint>()
        val cutoff = reference.minusMinutes(1)
        for (i in times.indices) {
            val value = values.getOrNull(i) ?: continue
            val time = times[i].toLocalDateTimeOrNull() ?: continue
            if (time.isBefore(cutoff)) continue
            result += RainPoint(time, value)
            if (result.size >= maxPoints) break
        }
        return result
    }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
        runCatching { LocalDateTime.parse(this) }.getOrNull()
}
