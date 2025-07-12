package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.runBlocking

class StepDaoWrapper(
    private val stepDao: StepDao
): DaoWrapper<StepEntity> {
    companion object {
        private val TAG = StepDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<StepEntity>>> = sequence {
        val lastId = runBlocking { stepDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                stepDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
        }
    }
    
    override suspend fun getAll(): List<StepEntity> {
        return stepDao.getAll()
    }

    override suspend fun insertEvent(entity: StepEntity) {
        stepDao.insertEvent(entity)
    }

    suspend fun upsertEvent(entity: StepEntity) {
        stepDao.upsertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<StepEntity>) {
        stepDao.insertEvents(entities)
    }

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        stepDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Step Data")
        stepDao.deleteAll()
    }

    override suspend fun getLast(): StepEntity? {
        return stepDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<StepEntity>::class.java).toList()
        if(list.isEmpty()) return IdRange(
            startId = 0,
            endId = 0,
        )

        insertEvents(list)
        return IdRange(
            startId = list.minOf { it.id },
            endId = list.maxOf { it.id }
        )
    }
}