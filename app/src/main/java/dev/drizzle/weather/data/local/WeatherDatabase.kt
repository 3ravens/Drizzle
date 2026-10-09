package dev.drizzle.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.drizzle.weather.data.local.entity.ForecastEntity
import dev.drizzle.weather.data.local.entity.SavedLocationEntity

@Database(
    entities = [SavedLocationEntity::class, ForecastEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
