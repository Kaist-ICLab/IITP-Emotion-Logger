package kaist.iclab.loggerstructure.core

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionUtil {
    private val TAG = javaClass.simpleName
    fun isPermissionAllowed(androidContext: Context, permissions: List<String>): Boolean {
        return permissions.all { permission ->
            val permissionStatus = ContextCompat.checkSelfPermission(androidContext, permission)
            when (permissionStatus) {
                PackageManager.PERMISSION_GRANTED -> true
                PackageManager.PERMISSION_DENIED -> false
                else -> {
                    Log.d(TAG, "Unknown permission_status: $permissionStatus")
                    false
                }
            }
        }
    }
}