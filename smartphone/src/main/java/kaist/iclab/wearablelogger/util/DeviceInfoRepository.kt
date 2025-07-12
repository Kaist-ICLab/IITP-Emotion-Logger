package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.data.UploadAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class DeviceInfoRepository(
    context: Context,
) {
    companion object {
        private val TAG = DeviceInfoRepository::class.simpleName
        private const val CAPABILITY_NAME = "data_collection"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)

    @SuppressLint("HardwareIds")
    val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    private val _watchUploadSchedule = MutableStateFlow<Long>(0L)
    val watchUploadSchedule = _watchUploadSchedule.asStateFlow()

    val phoneUploadSchedule = AlarmScheduler.nextAlarmSchedule.map { it[UploadAlarmReceiver::class.simpleName] ?: 0 }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0,
        )

    private val _isWearableCharging = MutableStateFlow(false)
    val isWearableCharging = _isWearableCharging.asStateFlow()

    private val _wearableBatteryLevel = MutableStateFlow(-1)
    val wearableBatteryLevel = _wearableBatteryLevel.asStateFlow()

    fun getWearablesFlow(): StateFlow<String?> = callbackFlow {
        val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val nodes = capabilityInfo.nodes.firstOrNull { it -> it.isNearby }
            trySend(nodes?.displayName)
            Log.d(TAG, "capabilityChangedListener: ${capabilityInfo.nodes}")
        }

        // 초기 상태 emit
        val initial = capabilityClient
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                Log.d(TAG, "OnSuccess()")
                for (node in nodes) {
                    Log.d("Capability", "Connected node with capability: ${node.displayName}")
                }
            }
            .await()

        Log.d(TAG, "Initial capabilityClient: ${initial.nodes}")
        val nodes = initial.nodes.firstOrNull { it -> it.isNearby }
        trySend(nodes?.displayName)
        capabilityClient.addListener(listener, CAPABILITY_NAME)

        awaitClose {
            capabilityClient.removeListener(listener)
        }
    }.distinctUntilChanged().stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = null
    )

    fun updateWatchUploadSchedule(timestamp: Long) {
        _watchUploadSchedule.value = timestamp
    }

    fun updateWatchBatteryState(isCharging: Boolean, batteryLevel: Int) {
        _isWearableCharging.value = isCharging
        _wearableBatteryLevel.value = batteryLevel
    }
}