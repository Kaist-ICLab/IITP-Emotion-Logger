package kaist.iclab.wearablelogger

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import kaist.iclab.loggerstructure.core.PermissionActivity
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.collector.core.AlarmReceiver
import kaist.iclab.wearablelogger.collector.core.BatteryStateReceiver
import kaist.iclab.wearablelogger.ui.SettingsScreen
import kaist.iclab.wearablelogger.ui.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "MainActivity"

class MainActivity : PermissionActivity() {
    private val settingsViewModel: SettingsViewModel by viewModel()
    private lateinit var batteryReceiver: BatteryStateReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup required permissions
        val permissionList = listOfNotNull(
            Manifest.permission.FOREGROUND_SERVICE,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
            Manifest.permission.BODY_SENSORS,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        requestPermissions.launch(permissionList.toTypedArray())

        batteryReceiver = BatteryStateReceiver()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel
            )
        }

        // Setup periodic upload worker
        AlarmScheduler.scheduleExactAlarm(this, AlarmReceiver::class.java)
    }

    override fun onResume() {
        super.onResume()

        // (re)start job if it was configured to collect data
        val isCollecting = settingsViewModel.isCollectorState.value
        if(isCollecting) settingsViewModel.startLogging()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        AlarmScheduler.cancelAlarm(this, AlarmReceiver::class.java)
    }
}

