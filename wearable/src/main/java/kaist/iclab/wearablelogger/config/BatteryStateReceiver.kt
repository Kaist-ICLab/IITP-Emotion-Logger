package kaist.iclab.wearablelogger.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import org.koin.java.KoinJavaComponent.inject

class BatteryStateReceiver : BroadcastReceiver() {
//    companion object {
//        private val TAG = BatteryStateReceiver::class.simpleName
//    }

    private val batteryStateRepository: BatteryStateRepository by inject(BatteryStateRepository::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        batteryStateRepository.updateBatteryState(isCharging, batteryLevel)
    }
}