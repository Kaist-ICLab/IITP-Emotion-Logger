package kaist.iclab.wearablelogger.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        private const val IS_STEP_COLLECTED = "IS_STEP_COLLECTED"
        private const val IS_ENV_COLLECTED = "IS_ENV_COLLECTED"
        private const val SYNC_TIME = "SYNC_TIME"
        private const val UPLOAD_TIME = "UPLOAD_TIME"
    }

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore("STATE")
    val bluetoothAddress: Flow<String> = context.dataStore.data.map { it[stringPreferencesKey(BLUETOOTH_ADDRESS)] ?: "None"}
    val isStepCollected: Flow<Boolean> = context.dataStore.data.map { it[booleanPreferencesKey(IS_STEP_COLLECTED)] == true }
    val isEnvCollected: Flow<Boolean> = context.dataStore.data.map { it[booleanPreferencesKey(IS_ENV_COLLECTED)] == true }

    val syncTime: Flow<Map<CollectorType, Long>> = context.dataStore.data
        .map { pref ->
            mapOf(
                CollectorType.HR to (pref[longPreferencesKey("${SYNC_TIME}_${CollectorType.HR.name}")] ?: -1),
                CollectorType.ACC to (pref[longPreferencesKey("${SYNC_TIME}_${CollectorType.ACC.name}")] ?: -1),
                CollectorType.PPG to (pref[longPreferencesKey("${SYNC_TIME}_${CollectorType.PPG.name}")] ?: -1),
                CollectorType.SKINTEMP to (pref[longPreferencesKey("${SYNC_TIME}_${CollectorType.SKINTEMP.name}")] ?: -1),
            )
        }
    val uploadTime: Flow<Map<CollectorType, Long>> = context.dataStore.data
        .map { pref ->
            mapOf(
                CollectorType.HR to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.HR.name}")] ?: -1),
                CollectorType.ACC to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.ACC.name}")] ?: -1),
                CollectorType.PPG to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.PPG.name}")] ?: -1),
                CollectorType.SKINTEMP to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.SKINTEMP.name}")] ?: -1),
                CollectorType.ENV to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.ENV.name}")] ?: -1),
                CollectorType.STEP to (pref[longPreferencesKey("${UPLOAD_TIME}_${CollectorType.STEP.name}")] ?: -1),
            )
        }

    suspend fun updateBluetoothAddress(address: String) {
        context.dataStore.edit { it[stringPreferencesKey(BLUETOOTH_ADDRESS)] = address }
    }

    suspend fun updateIsStepCollected(isCollected: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(IS_STEP_COLLECTED)] = isCollected }
    }

    suspend fun updateIsEnvCollected(isCollected: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(IS_ENV_COLLECTED)] = isCollected }
    }

    suspend fun updateSyncTime(collectorName: String, time: Long) {
        context.dataStore.edit { pref ->
            pref[longPreferencesKey("${SYNC_TIME}_$collectorName")] = time
        }
    }

    suspend fun updateUploadTime(collectorName: String, time: Long) {
        context.dataStore.edit { pref ->
            pref[longPreferencesKey("${UPLOAD_TIME}_$collectorName")] = time
        }
    }
}