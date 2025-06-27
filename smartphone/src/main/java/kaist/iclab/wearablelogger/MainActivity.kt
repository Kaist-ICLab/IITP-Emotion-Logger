package kaist.iclab.wearablelogger

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.PermissionActivity
import kaist.iclab.wearablelogger.step.SamsungHealthPermissionManager
import kaist.iclab.wearablelogger.ui.MainApp
import org.koin.android.ext.android.inject

private const val TAG = "MainActivity"

class MainActivity : PermissionActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val dataReceiver:DataReceiver by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request for android permission
        val permissionList = listOfNotNull(
            Manifest.permission.FOREGROUND_SERVICE,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) Manifest.permission.FOREGROUND_SERVICE_HEALTH else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
            Manifest.permission.BODY_SENSORS,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        requestPermissions.launch(permissionList.toTypedArray())

        // Request for Samsung Health Data SDK permission
        val permManager = SamsungHealthPermissionManager(this)
        permManager.request {
            res -> if(res) Log.d(TAG, "All permissions granted")
        }

        setContent {
            MainApp()
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(dataReceiver)
    }

//    override fun onPause() {
//        super.onPause()
//        dataClient.removeListener(dataReceiver)
//    }
}


