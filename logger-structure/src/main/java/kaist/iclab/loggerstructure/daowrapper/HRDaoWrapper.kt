package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.entity.HREntity
import kotlinx.coroutines.runBlocking

class HRDaoWrapper(
    private val hrDao: HRDao
): DaoWrapper<HREntity> {
    companion object {
        private val TAG = HRDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<HREntity>>> = sequence {
        val lastId = runBlocking { hrDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                hrDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
        }
    }

    override suspend fun getAll(): List<HREntity> {
        return hrDao.getAll()
    }

    override suspend fun insertEvent(entity: HREntity) {
        hrDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<HREntity>) {
        hrDao.insertEvents(entities)
    }

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        hrDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for HR Data")
        hrDao.deleteAll()
    }

    override suspend fun getLast(): HREntity? {
        return hrDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<HREntity>::class.java).toList()
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