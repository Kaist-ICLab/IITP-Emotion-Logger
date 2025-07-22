package kaist.iclab.loggerstructure.core

interface CollectorInterface {
    val key: String
    fun setup()
    fun isAvailable():Boolean
    fun startLogging()
    fun stopLogging()
    fun deleteBetween(startId: Long, endId: Long)
    fun flush()
}