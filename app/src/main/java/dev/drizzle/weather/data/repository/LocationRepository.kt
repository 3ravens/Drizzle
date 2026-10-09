package dev.drizzle.weather.data.repository

import dev.drizzle.weather.data.local.WeatherDao
import dev.drizzle.weather.data.local.entity.SavedLocationEntity
import dev.drizzle.weather.data.local.entity.toDomain
import dev.drizzle.weather.data.remote.GeocodingApi
import dev.drizzle.weather.data.settings.SettingsDataStore
import dev.drizzle.weather.domain.model.SavedLocation
import dev.drizzle.weather.domain.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocationRepository(
    private val geocodingApi: GeocodingApi,
    private val dao: WeatherDao,
    private val settings: SettingsDataStore,
) {

    /** Emits the active location, falling back to the first saved one. */
    fun observeActiveLocation(): Flow<SavedLocation?> =
        combine(dao.observeLocations(), settings.activeLocationId) { entities, activeId ->
            val locations = entities.map { it.toDomain() }
            locations.firstOrNull { it.id == activeId } ?: locations.firstOrNull()
        }

    fun observeLocations(): Flow<List<SavedLocation>> =
        dao.observeLocations().map { entities -> entities.map { it.toDomain() } }

    /** Ensures a "Current location" entry exists on first launch. */
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.countLocations() == 0) {
            val id = dao.insertLocation(
                SavedLocationEntity(
                    name = "Current location",
                    latitude = 0.0,
                    longitude = 0.0,
                    isCurrentLocation = true,
                    sortOrder = 0,
                ),
            )
            settings.setActiveLocationId(id)
        }
    }

    suspend fun updateCurrentLocation(id: Long, latitude: Double, longitude: Double, name: String) =
        withContext(Dispatchers.IO) { dao.updateLocation(id, latitude, longitude, name) }

    suspend fun setActive(id: Long) = settings.setActiveLocationId(id)

    suspend fun add(result: SearchResult): Long = withContext(Dispatchers.IO) {
        val id = dao.insertLocation(
            SavedLocationEntity(
                name = result.name,
                latitude = result.latitude,
                longitude = result.longitude,
                isCurrentLocation = false,
                sortOrder = 1,
            ),
        )
        settings.setActiveLocationId(id)
        id
    }

    suspend fun delete(location: SavedLocation) = withContext(Dispatchers.IO) {
        dao.deleteLocation(
            SavedLocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                isCurrentLocation = location.isCurrentLocation,
                sortOrder = 0,
            ),
        )
    }

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val response = geocodingApi.search(
            name = query.trim(),
            count = 8,
            language = "en",
            format = "json",
        )
        response.results.orEmpty().map {
            SearchResult(
                name = it.name,
                latitude = it.latitude,
                longitude = it.longitude,
                country = it.country,
                admin1 = it.admin1,
            )
        }
    }
}
