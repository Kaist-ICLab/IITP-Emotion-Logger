package kaist.iclab.wearablelogger

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kaist.iclab.loggerstructure.core.PermissionActivity
import kaist.iclab.wearablelogger.ui.SettingsScreen
import kaist.iclab.wearablelogger.uploader.SensorDataUploadWorker
import java.util.concurrent.TimeUnit

private const val TAG = "MainActivity"

class MainActivity : PermissionActivity() {
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

        setContent {
            SettingsScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        // Setup periodic upload worker
        scheduleSensorUploadWorker(this)
    }
}

fun scheduleSensorUploadWorker(context: Context) {
    Log.v(TAG, "scheduleSensorUploadWorker()")

    // Minimum period is 15 minutes
    val workRequest = PeriodicWorkRequestBuilder<SensorDataUploadWorker>(
        15, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "sensor_data_sync",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        workRequest
    )
}