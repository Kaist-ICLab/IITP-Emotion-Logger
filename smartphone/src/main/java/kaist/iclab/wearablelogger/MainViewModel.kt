package kaist.iclab.wearablelogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.wearablelogger.db.EventDao
import kaist.iclab.wearablelogger.db.EventEntity
import kaist.iclab.wearablelogger.db.RecentDao
import kaist.iclab.wearablelogger.db.RecentEntity
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    val eventDao: EventDao,
    stepDao: StepDao,
    val recentDao: RecentDao,
    val daoWrappers: List<DaoWrapper<EntityBase>>
) : ViewModel(){

    fun onClick() {
        CoroutineScope(Dispatchers.IO).launch {
            eventDao.insertEvent(
                EventEntity(timestamp = System.currentTimeMillis())
            )
        }
    }

    fun flush() {
        Log.v(TAG, "Flush all data")
        daoWrappers.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                it.deleteAll()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            recentDao.deleteAll()
        }
    }

    val eventsState: StateFlow<List<EventEntity>> =
        eventDao.getAllEvent().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = listOf()
        )

    val recentDataState: StateFlow<RecentEntity?> =
        recentDao.getLastEvent().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecentEntity(timestamp = -1, acc= "null", ppg="null", hr="null", skinTemp = "null")
        )

    val stepsState: StateFlow<StepEntity?> =
        stepDao.getLastByFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StepEntity(dataReceived = -1, startTime = -1, endTime = -1, step = 0)
        )
}
