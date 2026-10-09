package dev.drizzle.weather.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.drizzle.weather.DrizzleApp
import dev.drizzle.weather.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    locationRepository: LocationRepository,
) : ViewModel() {

    val activeLocationName: StateFlow<String> = locationRepository.observeActiveLocation()
        .map { it?.name ?: "—" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "—")

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DrizzleApp
                SettingsViewModel(app.container.locationRepository)
            }
        }
    }
}
