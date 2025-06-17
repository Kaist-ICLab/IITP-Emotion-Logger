package kaist.iclab.wearablelogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.wearablelogger.db.EventDao
import kaist.iclab.wearablelogger.db.EventEntity
import kaist.iclab.wearablelogger.db.RecentDao
import kaist.iclab.wearablelogger.db.RecentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//private const val TAG = "MainViewModel"

class MainViewModel(
    val eventDao: EventDao,
    recentDao: RecentDao,
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
        daoWrappers.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                it.deleteAll()
            }
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
            initialValue = RecentEntity(timestamp = -1, acc= "null", ppg="null", hr="null")
        )
}
