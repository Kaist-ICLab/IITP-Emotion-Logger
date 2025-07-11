package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kotlinx.coroutines.runBlocking

class AccDaoWrapper(
    private val accDao: AccDao
): DaoWrapper<AccEntity> {
    companion object {
        private val TAG = AccDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<AccEntity>>> = sequence {
        val lastId = runBlocking { accDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                accDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
        }
    }

    override suspend fun getAll(): List<AccEntity> {
        return accDao.getAll()
    }

    override suspend fun insertEvent(entity: AccEntity) {
        accDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<AccEntity>) {
        accDao.insertEvents(entities)
    }

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        accDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for ACC Data")
        accDao.deleteAll()
    }

    override suspend fun getLast(): AccEntity? {
        return accDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<AccEntity>::class.java).toList()
        insertEvents(list)
        return IdRange(
            startId = list.minOf { it.id },
            endId = list.maxOf { it.id }
        )
    }
}