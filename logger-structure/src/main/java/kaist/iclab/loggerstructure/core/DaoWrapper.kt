package kaist.iclab.loggerstructure.core

interface DaoWrapper<T: EntityBase> {
    suspend fun getBeforeLast(): Pair<Long, List<T>>

    suspend fun getAll(): List<T>

    suspend fun insertEvent(entity: T)

    suspend fun insertEvents(entities: List<T>)

    suspend fun deleteBefore(timestamp: Long)

    suspend fun deleteAll()

    suspend fun getLast(): T?

    suspend fun insertEventsFromJson(json: String)
}

interface EntityBase
