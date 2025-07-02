package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.wearablelogger.dao.StepDao
import kaist.iclab.wearablelogger.entity.StepEntity
import kaist.iclab.wearablelogger.dao.EnvDao
import kaist.iclab.wearablelogger.dao.RecentDao
import kaist.iclab.wearablelogger.entity.EnvEntity
import kaist.iclab.wearablelogger.entity.RecentEntity
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
    private val recentDao: RecentDao,
    private val daoWrappers: List<DaoWrapper<EntityBase>>
) : ViewModel(){
    fun flush() {
        Log.v(TAG, "Flush all data")
        daoWrappers.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                it.deleteAll()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            stepDao.deleteAll()
            recentDao.deleteAll()
            envDao.deleteAll()
        }
    }

    val recentDataState: StateFlow<RecentEntity?> =
        recentDao.getLastByFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = RecentEntity(
                timestamp = -1,
                acc = "null",
                ppg = "null",
                hr = "null",
                skinTemp = "null"
            )
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
}