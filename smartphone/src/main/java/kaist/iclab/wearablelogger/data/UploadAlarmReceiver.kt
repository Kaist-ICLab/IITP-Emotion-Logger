package kaist.iclab.wearablelogger.data

import kaist.iclab.loggerstructure.core.AlarmReceiver
import org.koin.java.KoinJavaComponent

class UploadAlarmReceiver: AlarmReceiver() {
    private val dataUploaderRepository: DataUploaderRepository by KoinJavaComponent.inject(
        DataUploaderRepository::class.java
    )

    override fun executeOnAlarm() {
        dataUploaderRepository.uploadSummaryData()
    }
}