package kaist.iclab.wearablelogger.collector.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class BatteryStateReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = BatteryStateReceiver::class.simpleName
        var isCharging = false
        var chargeStartTimestamp = -1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        if (isCharging) {
            Log.d(TAG, "Charging now...")
        } else {
            Log.d(TAG, "Not charging")
        }
    }
}