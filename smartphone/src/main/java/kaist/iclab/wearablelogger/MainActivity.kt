package kaist.iclab.wearablelogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.env.EnvCollectorService
import kaist.iclab.wearablelogger.step.SamsungHealthPermissionManager
import kaist.iclab.wearablelogger.step.StepCollectorService
import kaist.iclab.wearablelogger.ui.MainApp
import kaist.iclab.wearablelogger.ui.MainViewModel
import kaist.iclab.wearablelogger.util.UploadAlarmReceiver
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val stateRepository: StateRepository by inject()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result: Map<String, Boolean> ->
            Log.d(TAG, "$result")
            val allGranted = result.values.all { it }

            if(!allGranted) {
                Toast.makeText(this, "일부 권한이 거부되었습니다. 모든 권한을 허용해주셔야 정상적인 실험이 가능합니다.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            proceedAfterPermissionGranted()
        }

        setContent {
            MainApp(
                mainViewModel = mainViewModel
            )
        }

        Handler(Looper.getMainLooper()).post {
            checkAndRequestPermissions()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val context = this@MainActivity
            stateRepository.isStepCollected.first { isCollected ->
                if(isCollected) {
                    val intent = Intent(context, StepCollectorService::class.java)
                    ContextCompat.startForegroundService(context, intent)
                }
                true
            }
            stateRepository.isEnvCollected.first { isCollected ->
                if(isCollected) {
                    val intent = Intent(context, EnvCollectorService::class.java)
                    ContextCompat.startForegroundService(context, intent)
                }
                true
            }
        }

        // Setup periodic upload worker
        AlarmScheduler.scheduleExactAlarm(this, UploadAlarmReceiver::class.java, TimeUnit.MINUTES.toMillis(15))
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmScheduler.cancelAlarm(this, UploadAlarmReceiver::class.java)
    }

    private fun checkAndRequestPermissions() {
        mainViewModel.disableAll()

        // Request for android permission
        val permissionList = listOfNotNull(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
            Manifest.permission.BODY_SENSORS,
        )

        val permissionsToRequest = permissionList.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        Log.d(TAG, "permissionsToRequest: $permissionsToRequest")

        if (permissionsToRequest.isEmpty()) {
            proceedAfterPermissionGranted()
            return
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun proceedAfterPermissionGranted() {
        val permManager = SamsungHealthPermissionManager(this)
        permManager.request { res ->
            if (res) {
                mainViewModel.enableStep()
            }
        }

        mainViewModel.enableEnv()
    }
}


