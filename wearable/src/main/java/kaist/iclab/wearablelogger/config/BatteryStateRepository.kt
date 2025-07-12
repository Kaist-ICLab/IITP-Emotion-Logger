package kaist.iclab.wearablelogger.config

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryStateRepository {
    companion object {
        private val TAG = BatteryStateRepository::class.simpleName
    }

    private val _isCharging = MutableStateFlow(false)
    private val _batteryLevel = MutableStateFlow(-1)
    private val _chargeStartTimestamp = MutableStateFlow(0L)

    val isCharging = _isCharging.asStateFlow()
    val batteryLevel = _batteryLevel.asStateFlow()
    val chargeStartTimestamp = _chargeStartTimestamp.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            isCharging.collect { isCharging ->
                if (isCharging) {
                    _chargeStartTimestamp.value = System.currentTimeMillis()
                    Log.d(TAG, "Charging now...")
                } else {
                    Log.d(TAG, "Not charging")
                }
            }
        }
    }

    fun updateBatteryState(isCharging: Boolean, batteryLevel: Int) {
        _isCharging.value = isCharging
        _batteryLevel.value = batteryLevel
    }
}