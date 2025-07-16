package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.IdRange
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.entity.PpgEntity
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class PpgDaoWrapper(
    private val ppgDao: PpgDao
): DaoWrapper<PpgEntity> {
    companion object {
        private val TAG = PpgDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<PpgEntity>>> = sequence {
        val lastId = runBlocking { ppgDao.getLastId() ?: 0 }
        var startId = startId

        while(true) {
            val entries = runBlocking {
                ppgDao.getChunkBetween(startId, lastId, limit)
            }
            if(entries.isEmpty()) break

            val idRange = IdRange(startId = entries.minOf{ it.id }, endId = entries.maxOf { it.id  })
            startId = idRange.endId + 1

            yield(Pair(idRange, entries))
        }
    }

    override suspend fun getAll(): List<PpgEntity> {
        return ppgDao.getAll()
    }

    override suspend fun insertEvent(entity: PpgEntity) {
        ppgDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<PpgEntity>) {
        ppgDao.insertEvents(entities)
    }

    override suspend fun deleteBetween(startId: Long, endId: Long) {
        ppgDao.deleteBetween(startId, endId)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for PPG Data")
        ppgDao.deleteAll()
    }

    override suspend fun getLast(): PpgEntity? {
        return ppgDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String): IdRange {
        val list = Gson().fromJson(json, Array<PpgEntity>::class.java).toList()
        if(list.isEmpty()) return IdRange(
            startId = 0,
            endId = 0,
        )

        ppgDao.upsertEvents(list)
        return IdRange(
            startId = list.minOf { it.id },
            endId = list.maxOf { it.id }
        )
    }

    override suspend fun getSummary(): JsonArray {
        return Gson().toJsonTree(ppgDao.getSummary(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))).asJsonArray
    }
}