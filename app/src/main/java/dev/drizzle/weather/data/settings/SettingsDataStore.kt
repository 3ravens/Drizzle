package dev.drizzle.weather.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Persisted app settings. v1 only needs the active location id. */
class SettingsDataStore(private val context: Context) {

    private val activeLocationKey = longPreferencesKey("active_location_id")

    val activeLocationId: Flow<Long?> = context.dataStore.data.map { it[activeLocationKey] }

    suspend fun setActiveLocationId(id: Long) {
        context.dataStore.edit { it[activeLocationKey] = id }
    }
}
