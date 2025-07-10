package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kotlinx.coroutines.runBlocking

class SkinTempDaoWrapper(
    private val skinTempDao: SkinTempDao
): DaoWrapper<SkinTempEntity> {
    companion object {
        private val TAG = SkinTempDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(limit: Int): Sequence<Pair<Long, List<SkinTempEntity>>> = sequence {
        val lastTimestamp = runBlocking {
            skinTempDao.getLast()?.timestamp ?: 0
        }
        while(true) {
            val entries = runBlocking {
                skinTempDao.getChunkBefore(lastTimestamp, limit)
            }
            if(entries.isEmpty()) break
            val maxTime = entries.maxOf { it.timestamp }
            yield(Pair(maxTime, entries))
        }
    }

    override suspend fun getAll(): List<SkinTempEntity> {
        return skinTempDao.getAll()
    }

    override suspend fun insertEvent(entity: SkinTempEntity) {
        skinTempDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<SkinTempEntity>) {
        skinTempDao.insertEvents(entities)
    }

    override suspend fun deleteBefore(timestamp: Long) {
        return skinTempDao.deleteBefore(timestamp)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for SkinTemp Data")
        skinTempDao.deleteAll()
    }

    override suspend fun getLast(): SkinTempEntity? {
        return skinTempDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<SkinTempEntity>::class.java).toList()
        insertEvents(list)
    }
}