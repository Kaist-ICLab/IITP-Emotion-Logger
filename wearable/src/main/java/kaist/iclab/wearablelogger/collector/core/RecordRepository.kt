package kaist.iclab.wearablelogger.collector.core

import kaist.iclab.loggerstructure.dao.RecordDao
import kaist.iclab.loggerstructure.entity.RecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RecordRepository(
    private val recordDao: RecordDao
) {
    val recordStateFlow = recordDao.getAll().stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = listOf()
    )

    suspend fun addEvent(startTime: Long, endTime: Long, label: String) {
        recordDao.insertEvent(RecordEntity(
            startTime = startTime,
            endTime = endTime,
            label = label
        ))
    }

    suspend fun deleteEvent(id: Long) {
        recordDao.delete(id)
    }

    suspend fun flush() {
        recordDao.deleteAll()
    }
}