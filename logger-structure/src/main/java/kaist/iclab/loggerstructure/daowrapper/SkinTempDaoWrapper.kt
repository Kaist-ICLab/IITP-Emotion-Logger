package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kotlinx.coroutines.runBlocking

class SkinTempDaoWrapper(
    private val skinTempDao: SkinTempDao
): DaoWrapper<SkinTempEntity> {
    companion object {
        private val TAG = SkinTempDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<SkinTempEntity>>> = sequence {
        val lastId = runBlocking { skinTempDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                skinTempDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
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

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        skinTempDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for SkinTemp Data")
        skinTempDao.deleteAll()
    }

    override suspend fun getLast(): SkinTempEntity? {
        return skinTempDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<SkinTempEntity>::class.java).toList()
        if(list.isEmpty()) return IdRange(
            startId = 0,
            endId = 0,
        )

        skinTempDao.upsertEvents(list)
        return IdRange(
            startId = list.minOf { it.id },
            endId = list.maxOf { it.id }
        )
    }
}