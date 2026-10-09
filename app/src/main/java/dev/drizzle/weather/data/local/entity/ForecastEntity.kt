package dev.drizzle.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Raw Open-Meteo JSON payload for a location, plus when it was fetched. */
@Entity(tableName = "forecast_cache")
data class ForecastEntity(
    @PrimaryKey val locationId: Long,
    val payload: String,
    val updatedAt: Long,
)
