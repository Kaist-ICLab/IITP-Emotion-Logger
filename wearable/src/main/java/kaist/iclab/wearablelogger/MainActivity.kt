package kaist.iclab.wearablelogger

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kaist.iclab.loggerstructure.core.PermissionActivity
import kaist.iclab.wearablelogger.collector.core.AlarmScheduler
import kaist.iclab.wearablelogger.collector.core.BatteryStateReceiver
import kaist.iclab.wearablelogger.ui.SettingsScreen
import kaist.iclab.wearablelogger.ui.SettingsViewModel
import kaist.iclab.wearablelogger.uploader.SensorDataUploadWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

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
    }

    override fun onResume() {
        super.onResume()
        // Setup periodic upload worker
//        scheduleSensorUploadWorker()
        AlarmScheduler.scheduleExactAlarm(this)

        // (re)start job if it was configured to collect data
        val isCollecting = settingsViewModel.isCollectorState.value
        if(isCollecting) settingsViewModel.startLogging()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    private fun scheduleSensorUploadWorker() {
        Log.v(TAG, "scheduleSensorUploadWorker()")

        // Minimum period is 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<SensorDataUploadWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sensor_data_sync",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }
}

