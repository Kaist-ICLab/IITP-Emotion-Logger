package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.util.DataReceiver
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class StatusViewModel(
    private val stepDao: StepDao,
    private val envDao: EnvDao,
    private val daoWrappers: List<DaoWrapper<EntityBase>>,
    stateRepository: StateRepository,
) : ViewModel(){
    val syncTime: StateFlow<Map<CollectorType, Long>> =
        stateRepository.syncTime.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = mapOf()
        )

    val recentTimestamp: StateFlow<Long> = DataReceiver.recentTimestamp.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = -1
    )

    val recentHREntity: StateFlow<HREntity?> = DataReceiver.recentHREntity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = null
    )

    val recentAccEntity: StateFlow<AccEntity?> = DataReceiver.recentAccEntity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = null
    )

    val recentPpgEntity: StateFlow<PpgEntity?> = DataReceiver.recentPpgEntity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = null
    )

    val recentSkinTempEntity: StateFlow<SkinTempEntity?> = DataReceiver.recentSkinTempEntity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = null
    )

    val stepsState: StateFlow<StepEntity?> =
        stepDao.getLastByFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = StepEntity(dataReceived = -1, startTime = -1, endTime = -1, step = 0)
        )

    val environmentState: StateFlow<EnvEntity?> =
        envDao.getLastByFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = EnvEntity(
                timestamp = -1,
                temperature = 0.0,
                humidity = 0.0,
                co2 = 0,
                tvoc = 0
            )
        )

    fun flush() {
        Log.v(TAG, "Flush all data")
        daoWrappers.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                it.deleteAll()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            stepDao.deleteAll()
            envDao.deleteAll()
        }
    }
}