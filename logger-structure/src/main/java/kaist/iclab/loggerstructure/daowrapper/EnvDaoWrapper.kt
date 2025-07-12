package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.entity.EnvEntity
import kotlinx.coroutines.runBlocking

class EnvDaoWrapper(
    private val envDao: EnvDao
): DaoWrapper<EnvEntity> {
    companion object {
        private val TAG = EnvDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<EnvEntity>>> = sequence {
        val lastId = runBlocking { envDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                envDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
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

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        envDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Env Data")
        envDao.deleteAll()
    }

    override suspend fun getLast(): EnvEntity? {
        return envDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<EnvEntity>::class.java).toList()
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