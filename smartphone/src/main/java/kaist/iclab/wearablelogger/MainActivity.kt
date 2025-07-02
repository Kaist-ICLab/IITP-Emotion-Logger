package kaist.iclab.wearablelogger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.Wearable
import kaist.iclab.wearablelogger.step.SamsungHealthPermissionManager
import kaist.iclab.wearablelogger.ui.MainApp
import kaist.iclab.wearablelogger.ui.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

//private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val dataReceiver: DataReceiver by inject()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result: Map<String, Boolean> ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                proceedAfterPermissionGranted()
            } else {
                Toast.makeText(this, "일부 권한이 거부되었습니다. 모든 권한을 허용해주셔야 정상적인 실험이 가능합니다.", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            MainApp(
                mainViewModel = mainViewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(dataReceiver)

        Handler(Looper.getMainLooper()).post {
            checkAndRequestPermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(dataReceiver)
    }

    private fun checkAndRequestPermissions() {
        mainViewModel.disableAll()
        // Request for android permission
        val permissionList = listOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else null,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        val permissionsToRequest = permissionList.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

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


