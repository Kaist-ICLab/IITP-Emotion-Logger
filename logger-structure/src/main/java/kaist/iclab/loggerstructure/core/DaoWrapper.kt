package kaist.iclab.loggerstructure.core

import com.google.gson.JsonArray

interface DaoWrapper<T: EntityBase> {
    suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<Pair<IdRange, List<T>>>

    suspend fun getSummary(): JsonArray

    suspend fun getAll(): List<T>

    suspend fun insertEvent(entity: T)

    suspend fun insertEvents(entities: List<T>)

    suspend fun deleteBetween(startId: Long, endId: Long)

    suspend fun deleteAll()

    suspend fun getLast(): T?

    suspend fun insertEventsFromJson(json: String): IdRange
}

interface EntityBase
