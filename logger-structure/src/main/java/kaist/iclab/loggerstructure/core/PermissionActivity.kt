package kaist.iclab.loggerstructure.core

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

private const val TAG = "PermissionActivity"

open class PermissionActivity: ComponentActivity() {
    val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        Log.d(TAG, it.toString())
    }
}