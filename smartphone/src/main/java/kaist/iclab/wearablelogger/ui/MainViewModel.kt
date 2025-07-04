package kaist.iclab.wearablelogger.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kaist.iclab.wearablelogger.blu.EnvCollectorService
import kaist.iclab.wearablelogger.step.StepCollectorService
import kaist.iclab.wearablelogger.util.DeviceInfoRepository
import kaist.iclab.wearablelogger.util.StateRepository
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "MainViewModel"

class MainViewModel(): ViewModel() {
    private val deviceInfoRepository: DeviceInfoRepository by inject(DeviceInfoRepository::class.java)
    private val stateRepository: StateRepository by inject(StateRepository::class.java)

    val deviceId = deviceInfoRepository.deviceId
    val bluetoothDeviceAddress = stateRepository.bluetoothAddress
    val wearables = deviceInfoRepository.getWearablesFlow()

    var isStepAvailable by mutableStateOf(false)
        private set

    var isStepRunning by mutableStateOf(false)
        private set

    var isEnvAvailable by mutableStateOf(false)
        private set

    var isEnvRunning by mutableStateOf(false)
        private set
    
    fun disableAll() {
        isStepAvailable = false
        isEnvAvailable = false
    }

    fun enableStep() {
        isStepAvailable = true
    }

    fun enableEnv() {
        isEnvAvailable = true
    }

    fun toggleStepRunning(context: Context) {
        val intent = Intent(context, StepCollectorService::class.java)
        if(isStepRunning) {
            context.stopService(intent)
            Log.d(TAG, "Stop StepCollectorService")
        } else {
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "Start StepCollectorService")
        }

        isStepRunning = !isStepRunning
    }
    
    fun toggleEnvRunning(context: Context) {
        val intent = Intent(context, EnvCollectorService::class.java)
        if(isEnvRunning) {
            context.stopService(intent)
            Log.d(TAG, "Stop DataCollectionService")
        } else {
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "Start DataCollectionService")
        }

        isEnvRunning = !isEnvRunning
    }
}