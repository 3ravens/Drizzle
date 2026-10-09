package dev.drizzle.weather.ui.overview

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.drizzle.weather.DrizzleApp
import dev.drizzle.weather.data.repository.CachedForecast
import dev.drizzle.weather.data.repository.LocationRepository
import dev.drizzle.weather.data.repository.RefreshResult
import dev.drizzle.weather.data.repository.WeatherRepository
import dev.drizzle.weather.domain.model.SavedLocation
import dev.drizzle.weather.location.LocationProvider
import dev.drizzle.weather.util.TimeFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow<OverviewUiState>(OverviewUiState.Loading)
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private var active: SavedLocation? = null
    private var lastLoadKey: String? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            locationRepository.ensureSeeded()
            locationRepository.observeActiveLocation().collect { location ->
                if (location == null) return@collect
                active = location
                load(location, force = false)
            }
        }
    }

    fun refresh() {
        active?.let { load(it, force = true) }
    }

    /** Called after the user answers the runtime location-permission dialog. */
    fun onPermissionResult() {
        refresh()
    }

    private fun load(location: SavedLocation, force: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _refreshing.value = true

            val resolved = resolveLocation(location)
            active = resolved

            if (resolved.isCurrentLocation && resolved.latitude == 0.0 && resolved.longitude == 0.0) {
                _state.value = if (locationProvider.hasPermission()) {
                    OverviewUiState.Error("Couldn't get your location. Add a city instead.")
                } else {
                    OverviewUiState.NeedsLocationPermission
                }
                _refreshing.value = false
                return@launch
            }

            val key = "${resolved.id}:${resolved.latitude}:${resolved.longitude}"
            if (!force && key == lastLoadKey) {
                _refreshing.value = false
                return@launch
            }
            lastLoadKey = key

            weatherRepository.cached(resolved.id)?.let { cached ->
                _state.value = cached.toSuccess(resolved.name, stale = true, failed = false)
            }

            when (val result = weatherRepository.refresh(resolved)) {
                is RefreshResult.Success ->
                    _state.value = result.cached.toSuccess(resolved.name, stale = false, failed = false)

                is RefreshResult.Failure -> {
                    val cached = result.cached
                    _state.value = if (cached != null) {
                        cached.toSuccess(resolved.name, stale = true, failed = true)
                    } else {
                        OverviewUiState.Error(result.message)
                    }
                }
            }
            _refreshing.value = false
        }
    }

    /** Refreshes the device fix for the "Current location" entry when needed. */
    private suspend fun resolveLocation(location: SavedLocation): SavedLocation {
        if (!location.isCurrentLocation || !locationProvider.hasPermission()) return location

        val fix = locationProvider.currentLocation() ?: return location
        val isEmpty = location.latitude == 0.0 && location.longitude == 0.0
        val moved = distanceMeters(location.latitude, location.longitude, fix.latitude, fix.longitude)

        if (isEmpty || moved > 1_000f) {
            val name = locationProvider.reverseGeocode(fix.latitude, fix.longitude)
                ?: location.name.ifBlank { "Current location" }
            locationRepository.updateCurrentLocation(location.id, fix.latitude, fix.longitude, name)
            return location.copy(latitude = fix.latitude, longitude = fix.longitude, name = name)
        }
        return location
    }

    private fun CachedForecast.toSuccess(
        name: String,
        stale: Boolean,
        failed: Boolean,
    ): OverviewUiState.Success = OverviewUiState.Success(
        locationName = name,
        forecast = forecast,
        updatedAt = updatedAt,
        isStale = stale || TimeFormat.isStale(updatedAt),
        refreshFailed = failed,
    )

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DrizzleApp
                OverviewViewModel(
                    weatherRepository = app.container.weatherRepository,
                    locationRepository = app.container.locationRepository,
                    locationProvider = app.container.locationProvider,
                )
            }
        }
    }
}
