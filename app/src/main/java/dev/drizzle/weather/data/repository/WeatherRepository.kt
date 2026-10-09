package dev.drizzle.weather.data.repository

import dev.drizzle.weather.data.local.WeatherDao
import dev.drizzle.weather.data.local.entity.ForecastEntity
import dev.drizzle.weather.data.mapper.WeatherMapper
import dev.drizzle.weather.data.remote.OpenMeteoApi
import dev.drizzle.weather.data.remote.dto.OpenMeteoResponse
import dev.drizzle.weather.domain.model.SavedLocation
import dev.drizzle.weather.domain.model.WeatherForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant

/** A forecast plus the moment it was fetched. */
data class CachedForecast(
    val forecast: WeatherForecast,
    val updatedAt: Instant,
)

sealed interface RefreshResult {
    data class Success(val cached: CachedForecast) : RefreshResult
    data class Failure(val message: String, val cached: CachedForecast?) : RefreshResult
}

class WeatherRepository(
    private val api: OpenMeteoApi,
    private val dao: WeatherDao,
    private val json: Json,
) {

    /** Returns the last successfully cached forecast, or null. */
    suspend fun cached(locationId: Long): CachedForecast? = withContext(Dispatchers.IO) {
        val entity = dao.getForecast(locationId) ?: return@withContext null
        runCatching {
            val response = json.decodeFromString(OpenMeteoResponse.serializer(), entity.payload)
            CachedForecast(WeatherMapper.map(response), Instant.ofEpochMilli(entity.updatedAt))
        }.getOrNull()
    }

    /** Fetches a fresh forecast, caching it on success. */
    suspend fun refresh(location: SavedLocation): RefreshResult = withContext(Dispatchers.IO) {
        try {
            val response = fetch(location)
            val payload = json.encodeToString(OpenMeteoResponse.serializer(), response)
            val now = System.currentTimeMillis()
            dao.upsertForecast(ForecastEntity(location.id, payload, now))
            RefreshResult.Success(CachedForecast(WeatherMapper.map(response), Instant.ofEpochMilli(now)))
        } catch (e: Exception) {
            RefreshResult.Failure(e.toUserMessage(), cached(location.id))
        }
    }

    private suspend fun fetch(location: SavedLocation): OpenMeteoResponse {
        return try {
            api.getForecast(
                latitude = location.latitude,
                longitude = location.longitude,
                current = OpenMeteoApi.CURRENT,
                hourly = OpenMeteoApi.HOURLY,
                minutely15 = OpenMeteoApi.MINUTELY,
                daily = OpenMeteoApi.DAILY,
                timezone = "auto",
                forecastDays = 2,
            )
        } catch (e: HttpException) {
            if (e.code() == 400) {
                // minutely_15 unsupported for this region — retry without it.
                api.getForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    current = OpenMeteoApi.CURRENT,
                    hourly = OpenMeteoApi.HOURLY,
                    minutely15 = null,
                    daily = OpenMeteoApi.DAILY,
                    timezone = "auto",
                    forecastDays = 2,
                )
            } else {
                throw e
            }
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is IOException -> "No internet connection"
        is HttpException -> "Weather service error (${code()})"
        else -> message ?: "Something went wrong"
    }
}
