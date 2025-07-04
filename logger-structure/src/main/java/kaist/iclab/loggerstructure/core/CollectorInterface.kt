package kaist.iclab.loggerstructure.core

interface CollectorInterface {
    val key: String
    fun setup()
    suspend fun getStatus(): Boolean
    fun isAvailable():Boolean
    fun startLogging()
    fun stopLogging()
    suspend fun stringifyData(): Pair<String, Long>
    fun flushBefore(timestamp: Long)
    fun flush()
}