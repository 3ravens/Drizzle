package dev.drizzle.weather.data.remote

import dev.drizzle.weather.data.remote.dto.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("minutely_15") minutely15: String?,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int,
    ): OpenMeteoResponse

    companion object {
        const val CURRENT =
            "temperature_2m,apparent_temperature,relative_humidity_2m,is_day,precipitation,weather_code,wind_speed_10m"
        const val HOURLY =
            "temperature_2m,precipitation,precipitation_probability,weather_code"
        const val DAILY =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset"
        const val MINUTELY = "precipitation"
    }
}
