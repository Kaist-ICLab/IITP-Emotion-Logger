package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.entity.EnvEntity
import kotlinx.coroutines.runBlocking

class EnvDaoWrapper(
    private val envDao: EnvDao
): DaoWrapper<EnvEntity> {
    companion object {
        private val TAG = EnvDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(limit: Int): Sequence<Pair<Long, List<EnvEntity>>> = sequence {
        val lastTimestamp = runBlocking {
            envDao.getLast()?.timestamp ?: 0
        }
        while(true) {
            val entries = runBlocking {
                envDao.getChunkBefore(lastTimestamp, limit)
            }
            if(entries.isEmpty()) break
            val maxTime = entries.maxOf { it.timestamp }
            yield(Pair(maxTime, entries))
        }
    }
    
    override suspend fun getAll(): List<EnvEntity> {
        return envDao.getAll()
    }

    override suspend fun insertEvent(entity: EnvEntity) {
        envDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<EnvEntity>) {
        envDao.insertEvents(entities)
    }

    override suspend fun deleteBefore(id: Long) {
        envDao.deleteBefore(id)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Env Data")
        envDao.deleteAll()
    }

    override suspend fun getLast(): EnvEntity? {
        return envDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<EnvEntity>::class.java).toList()
        insertEvents(list)
    }
}