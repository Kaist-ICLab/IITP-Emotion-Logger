package kaist.iclab.wearablelogger.uploader

import kaist.iclab.loggerstructure.core.AlarmReceiver
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import org.koin.java.KoinJavaComponent

class RecentAlarmReceiver: AlarmReceiver() {
    private val collectorRepository: CollectorRepository by KoinJavaComponent.inject(
        CollectorRepository::class.java
    )

    override fun executeOnAlarm() {
        collectorRepository.uploadRecent()
    }
}