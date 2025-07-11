package kaist.iclab.wearablelogger.util

import kaist.iclab.loggerstructure.core.AlarmReceiver
import kaist.iclab.wearablelogger.data.DataUploaderRepository
import org.koin.java.KoinJavaComponent

class UploadAlarmReceiver: AlarmReceiver() {
    private val dataUploaderRepository: DataUploaderRepository by KoinJavaComponent.inject(
        DataUploaderRepository::class.java
    )

    override fun executeOnAlarm() {
        dataUploaderRepository.uploadFullData()
    }
}