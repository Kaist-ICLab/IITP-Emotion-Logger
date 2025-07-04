package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeviceInfoRepository(
    private val context: Context,
) {
    companion object {
        private val TAG = DeviceInfoRepository::class.simpleName
    }

    @SuppressLint("HardwareIds")
    val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    fun getWearablesFlow(): Flow<Set<Node>> = callbackFlow {
        val capabilityClient = Wearable.getCapabilityClient(context)
        val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            trySend(capabilityInfo.nodes)
            Log.d(TAG, "capabilityChangedListener: ${capabilityInfo.nodes}")
        }

        // 초기 상태 emit
        val initial = capabilityClient
            .getCapability("my_wearable_capability", CapabilityClient.FILTER_ALL)
            .await()
        Log.d(TAG, "Initial capabilityClient: ${initial.nodes}")
        trySend(initial.nodes)

        capabilityClient.addListener(listener, "my_wearable_capability")

        awaitClose {
            capabilityClient.removeListener(listener)
        }
    }
}