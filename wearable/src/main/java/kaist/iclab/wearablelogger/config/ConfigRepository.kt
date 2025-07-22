package kaist.iclab.wearablelogger.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConfigRepository(private val androidContext: Context) {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore("CONFIG")
    val isCollectingFlow: Flow<Boolean> = androidContext.dataStore.data
        .map{ preferences ->
            preferences[booleanPreferencesKey("isCollecting")] == true
        }

    val pidFlow: Flow<String> = androidContext.dataStore.data
        .map { it[stringPreferencesKey("pid")] ?: "오은" }

    val labelFlow: Flow<String> = androidContext.dataStore.data
        .map { it[stringPreferencesKey("label")] ?: "A" }

    suspend fun updateCollectorStatus(status: Boolean){
        androidContext.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("isCollecting")] = status
        }
    }

    suspend fun updatePid(name: String) {
        androidContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("pid")] = name
        }
    }

    suspend fun updateLabel(label: String) {
        androidContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("label")] = label
        }
    }
}