package kaist.iclab.wearablelogger

import android.Manifest
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.wearable.Wearable
import kaist.iclab.wearablelogger.step.SamsungHealthPermissionManager
import kaist.iclab.wearablelogger.ui.MainApp
import kaist.iclab.wearablelogger.ui.MainViewModel
import kaist.iclab.wearablelogger.util.DataReceiver
import kaist.iclab.wearablelogger.util.SensorDataUploadWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = this::class.simpleName
    }

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val dataReceiver: DataReceiver by inject()
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
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(dataReceiver)

        // Setup periodic upload worker
        scheduleSensorUploadWorker()
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(dataReceiver)
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
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
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
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val hasBackgroundSensorPermission =
//                ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS_BACKGROUND) == PackageManager.PERMISSION_GRANTED
//
//            Log.d(TAG, "BODY_SENSORS_BACKGROUND: $hasBackgroundSensorPermission")
//            if(!hasBackgroundSensorPermission)
//                this.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
//        }

        val permManager = SamsungHealthPermissionManager(this)
        permManager.request { res ->
            if (res) {
                mainViewModel.enableStep()
            }
        }

        mainViewModel.enableEnv()
    }

    private fun scheduleSensorUploadWorker() {
        Log.v(TAG, "scheduleSensorUploadWorker()")

        // Minimum period is 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<SensorDataUploadWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sensor_data_sync",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }
}


