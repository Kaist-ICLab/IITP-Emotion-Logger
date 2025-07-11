package kaist.iclab.wearablelogger.collector.core

import kaist.iclab.loggerstructure.core.AlarmReceiver
import org.koin.java.KoinJavaComponent

class RecentAlarmReceiver: AlarmReceiver() {
    private val collectorRepository: CollectorRepository by KoinJavaComponent.inject(
        CollectorRepository::class.java
    )

    override fun executeOnAlarm() {
        collectorRepository.uploadRecent()
    }
}