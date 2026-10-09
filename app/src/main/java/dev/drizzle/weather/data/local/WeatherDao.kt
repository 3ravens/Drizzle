package dev.drizzle.weather.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.drizzle.weather.data.local.entity.ForecastEntity
import dev.drizzle.weather.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM saved_locations ORDER BY isCurrentLocation DESC, sortOrder ASC, name ASC")
    fun observeLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun countLocations(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: SavedLocationEntity): Long

    @Query("UPDATE saved_locations SET latitude = :latitude, longitude = :longitude, name = :name WHERE id = :id")
    suspend fun updateLocation(id: Long, latitude: Double, longitude: Double, name: String)

    @Delete
    suspend fun deleteLocation(entity: SavedLocationEntity)

    @Query("SELECT * FROM forecast_cache WHERE locationId = :locationId")
    suspend fun getForecast(locationId: Long): ForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertForecast(entity: ForecastEntity)
}
