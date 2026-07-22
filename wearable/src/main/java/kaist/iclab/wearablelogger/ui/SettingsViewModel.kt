package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import kaist.iclab.loggerstructure.entity.RecordEntity
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import kaist.iclab.wearablelogger.collector.core.RecordRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val configRepository: ConfigRepository,
    private val recordRepository: RecordRepository,
    private val collectorRepository: CollectorRepository
): ViewModel() {
    companion object {
        private val TAG = SettingsViewModel::class.simpleName
    }

    private var startTime = -1L

    val pidState: StateFlow<String> =
        configRepository.pidFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = "박규연"
            )

    val labelState: StateFlow<String> =
        configRepository.labelFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = "A"
            )

    val recordState: StateFlow<List<RecordEntity>> = recordRepository.recordStateFlow

    val isCollectorState: StateFlow<Boolean> =
        configRepository.isCollectingFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = false
            )

    fun startLogging() {
        collectorRepository.start()
        CoroutineScope(Dispatchers.IO).launch{
            configRepository.updateCollectorStatus(true)
        }
        startTime = System.currentTimeMillis()
    }

    fun stopLogging(){
        collectorRepository.stop()
        CoroutineScope(Dispatchers.IO).launch{
            configRepository.updateCollectorStatus(false)
            recordRepository.addEvent(startTime, System.currentTimeMillis(), labelState.value)
        }
    }

    fun updatePid(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            configRepository.updatePid(name)
        }
    }

    fun updateLabel(label: String) {
        Log.d(TAG, "updateLabel: $label")
        CoroutineScope(Dispatchers.IO).launch {
            configRepository.updateLabel(label)
        }
    }

    fun deleteRecord(recordEntity: RecordEntity) {
        Log.d(TAG, "recordEntity: $recordEntity")
        CoroutineScope(Dispatchers.IO).launch {
            recordRepository.deleteEvent(recordEntity.id)
            collectorRepository.deleteBetween(recordEntity.startTime, recordEntity.endTime)
        }
    }

    fun flush(){
        CoroutineScope(Dispatchers.IO).launch {
            collectorRepository.flush()
            recordRepository.flush()
        }
    }
}