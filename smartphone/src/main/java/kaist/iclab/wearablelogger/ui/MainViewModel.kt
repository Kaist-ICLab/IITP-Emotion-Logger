package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.wearablelogger.dao.EnvironmentDao
import kaist.iclab.wearablelogger.dao.RecentDao
import kaist.iclab.wearablelogger.entity.EnvironmentEntity
import kaist.iclab.wearablelogger.entity.RecentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    stepDao: StepDao,
    environmentDao: EnvironmentDao,
    val recentDao: RecentDao,
    val daoWrappers: List<DaoWrapper<EntityBase>>
) : ViewModel(){
    fun flush() {
        Log.v(TAG, "Flush all data")
        daoWrappers.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                it.deleteAll()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            recentDao.deleteAll()
            environmentDao.deleteAll()
        }
    }

    val recentDataState: StateFlow<RecentEntity?> =
        recentDao.getLastEvent().stateIn(
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

    val environmentState: StateFlow<EnvironmentEntity?> =
        environmentDao.getLastEvent().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = EnvironmentEntity(
                timestamp = -1,
                temperature = 0.0,
                humidity = 0.0,
                co2 = 0,
                tvoc = 0
            )
        )
}