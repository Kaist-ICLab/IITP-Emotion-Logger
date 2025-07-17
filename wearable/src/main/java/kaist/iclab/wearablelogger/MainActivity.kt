package kaist.iclab.wearablelogger

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import kaist.iclab.loggerstructure.core.PermissionActivity
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.ui.SettingsScreen
import kaist.iclab.wearablelogger.ui.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "MainActivity"

class MainActivity : PermissionActivity() {
    private val settingsViewModel: SettingsViewModel by viewModel()
    private val configRepository: ConfigRepository by inject()

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
            SettingsScreen(
                settingsViewModel = settingsViewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        // (re)start job if it was configured to collect data
        CoroutineScope(Dispatchers.IO).launch {
            configRepository.isCollectingFlow.first { isCollecting ->
                Log.d(TAG, "isCollecting: $isCollecting")
                if (isCollecting) settingsViewModel.startLogging()
                true
            }
        }
    }
}

