package kaist.iclab.wearablelogger.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.env.EnvCollectorService
import kaist.iclab.wearablelogger.step.StepCollectorService
import kaist.iclab.wearablelogger.util.DataReceiver
import kaist.iclab.wearablelogger.util.DeviceInfoRepository
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val TAG = "MainViewModel"

class MainViewModel(
    stepDao: StepDao,
    envDao: EnvDao,
    deviceInfoRepository: DeviceInfoRepository,
    stateRepository: StateRepository,
): ViewModel() {

    val deviceId = deviceInfoRepository.deviceId
    val bluetoothDeviceAddress = stateRepository.bluetoothAddress
    val wearables = deviceInfoRepository.getWearablesFlow()

    var isStepAvailable by mutableStateOf(false)
        private set

    val isStepRunning: StateFlow<Boolean> = getStateFlowFromFlow(stateRepository.isStepCollected, initialValue = false)

    var isEnvAvailable by mutableStateOf(false)
        private set

    var isEnvRunning: StateFlow<Boolean> = getStateFlowFromFlow(stateRepository.isEnvCollected, initialValue = false)

    val recentTimestamp: StateFlow<Long> = getStateFlowFromFlow(DataReceiver.recentTimestamp, initialValue = -1)
    val syncTime: StateFlow<Map<CollectorType, Long>> = getStateFlowFromFlow(stateRepository.syncTime, initialValue = mapOf())
    val uploadTime: StateFlow<Map<CollectorType, Long>> = getStateFlowFromFlow(stateRepository.uploadTime, initialValue = mapOf())

    val recentHREntity: StateFlow<HREntity?> = getStateFlowFromFlow(DataReceiver.recentHREntity, initialValue = null)
    val recentAccEntity: StateFlow<AccEntity?> = getStateFlowFromFlow(DataReceiver.recentAccEntity, initialValue = null)
    val recentPpgEntity: StateFlow<PpgEntity?> = getStateFlowFromFlow(DataReceiver.recentPpgEntity, initialValue = null)
    val recentSkinTempEntity: StateFlow<SkinTempEntity?> = getStateFlowFromFlow(DataReceiver.recentSkinTempEntity, initialValue = null)

    val recentStepEntity: StateFlow<StepEntity?> = getStateFlowFromFlow(stepDao.getLastByFlow(), initialValue = null)
    val recentEnvEntity: StateFlow<EnvEntity?> = getStateFlowFromFlow(envDao.getLastByFlow(), initialValue = null)
    
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
        if(isStepRunning.value) {
            context.stopService(intent)
            Log.d(TAG, "Stop StepCollectorService")
        } else {
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "Start StepCollectorService")
        }
    }
    
    fun toggleEnvRunning(context: Context) {
        val intent = Intent(context, EnvCollectorService::class.java)
        if(isEnvRunning.value) {
            context.stopService(intent)
            Log.d(TAG, "Stop EnvCollectorService")
        } else {
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "Start EnvCollectorService")
        }
    }

    private fun <T> getStateFlowFromFlow(flow: Flow<T>, initialValue: T): StateFlow<T> {
        return flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = initialValue
        )
    }
}