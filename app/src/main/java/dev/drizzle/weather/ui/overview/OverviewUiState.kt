package dev.drizzle.weather.ui.overview

import dev.drizzle.weather.domain.model.WeatherForecast
import java.time.Instant

sealed interface OverviewUiState {

    data object Loading : OverviewUiState

    data object NeedsLocationPermission : OverviewUiState

    data class Error(val message: String) : OverviewUiState

    data class Success(
        val locationName: String,
        val forecast: WeatherForecast,
        val updatedAt: Instant,
        val isStale: Boolean,
        val refreshFailed: Boolean,
    ) : OverviewUiState
}
