package kaist.iclab.loggerstructure.core

interface CollectorInterface {
    val key: String
    fun setup()
    suspend fun getStatus(): Boolean
    fun isAvailable():Boolean
    fun startLogging()
    fun stopLogging()
    suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String>
    fun deleteBetween(startId: Long, endId: Long)
    fun flush()
}