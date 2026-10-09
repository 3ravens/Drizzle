package dev.drizzle.weather.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.drizzle.weather.DrizzleApp
import dev.drizzle.weather.data.repository.LocationRepository
import dev.drizzle.weather.domain.model.SavedLocation
import dev.drizzle.weather.domain.model.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    val locations: StateFlow<List<SavedLocation>> = locationRepository.observeLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _query.value = value
        searchJob?.cancel()
        if (value.isBlank()) {
            _results.value = emptyList()
            _searching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _searching.value = true
            delay(300) // simple debounce
            runCatching { locationRepository.search(value) }
                .onSuccess { _results.value = it }
                .onFailure { _results.value = emptyList() }
            _searching.value = false
        }
    }

    fun add(result: SearchResult) {
        viewModelScope.launch { locationRepository.add(result) }
    }

    fun select(location: SavedLocation) {
        viewModelScope.launch { locationRepository.setActive(location.id) }
    }

    fun delete(location: SavedLocation) {
        viewModelScope.launch { locationRepository.delete(location) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DrizzleApp
                LocationsViewModel(app.container.locationRepository)
            }
        }
    }
}
