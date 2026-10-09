package dev.drizzle.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drizzle.weather.domain.model.SavedLocation

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean,
    val sortOrder: Int,
)

fun SavedLocationEntity.toDomain(): SavedLocation = SavedLocation(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    isCurrentLocation = isCurrentLocation,
)
