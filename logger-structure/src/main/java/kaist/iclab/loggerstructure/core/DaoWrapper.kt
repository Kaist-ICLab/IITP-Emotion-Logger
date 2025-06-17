package kaist.iclab.loggerstructure.core

interface EntityBase

interface DaoWrapper<T: EntityBase> {
    suspend fun getAll(): List<T>

    suspend fun insertEvent(entity: T)

    suspend fun insertEvents(entities: List<T>)

    suspend fun deleteAll()

    suspend fun getLast(): T

    suspend fun insertEventsFromJson(json: String)
}