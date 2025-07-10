package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kotlinx.coroutines.runBlocking

class AccDaoWrapper(
    private val accDao: AccDao
): DaoWrapper<AccEntity> {
    companion object {
        private val TAG = AccDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(limit: Int): Sequence<Pair<Long, List<AccEntity>>> = sequence {
        val lastTimestamp = runBlocking {
            accDao.getLast()?.timestamp ?: 0
        }
        while(true) {
            val entries = runBlocking {
                accDao.getChunkBefore(lastTimestamp, limit)
            }
            if(entries.isEmpty()) break
            val maxTime = entries.maxOf { it.timestamp }
            yield(Pair(maxTime, entries))
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

    override suspend fun deleteBefore(id: Long) {
        accDao.deleteBefore(id)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for ACC Data")
        accDao.deleteAll()
    }

    override suspend fun getLast(): AccEntity? {
        return accDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<AccEntity>::class.java).toList()
        insertEvents(list)
    }
}