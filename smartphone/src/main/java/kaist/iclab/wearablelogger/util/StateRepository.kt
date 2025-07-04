package kaist.iclab.wearablelogger.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kaist.iclab.loggerstructure.util.CollectorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.to

class StateRepository(private val context: Context) {
    companion object {
        private const val BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS"
    }

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore("STATE")
    val bluetoothAddress: Flow<String> = context.dataStore.data.map { it[stringPreferencesKey(BLUETOOTH_ADDRESS)] ?: "None"}
    val syncTime: Flow<Map<CollectorType, Long>> = context.dataStore.data
        .map { pref ->
            mapOf(
                CollectorType.HR to (pref[longPreferencesKey(CollectorType.HR.name)] ?: -1),
                CollectorType.ACC to (pref[longPreferencesKey(CollectorType.ACC.name)] ?: -1),
                CollectorType.PPG to (pref[longPreferencesKey(CollectorType.PPG.name)] ?: -1),
                CollectorType.SKINTEMP to (pref[longPreferencesKey(CollectorType.SKINTEMP.name)] ?: -1),
            )
        }

    suspend fun updateBluetoothAddress(address: String) {
        context.dataStore.edit { it[stringPreferencesKey(BLUETOOTH_ADDRESS)] = address }
    }

    suspend fun updateSyncTime(collectorName: String, time: Long) {
        context.dataStore.edit { pref ->
            pref[longPreferencesKey(collectorName)] = time
        }
    }
}