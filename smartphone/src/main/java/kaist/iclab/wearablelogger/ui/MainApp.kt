package kaist.iclab.wearablelogger.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kaist.iclab.wearablelogger.MainViewModel
import kaist.iclab.wearablelogger.db.EventEntity
import kaist.iclab.wearablelogger.db.RecentEntity
import kaist.iclab.wearablelogger.step.CollectorService
import kaist.iclab.loggerstructure.entity.StepEntity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainApp(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val events = mainViewModel.eventsState.collectAsState().value
    val recentData = mainViewModel.recentDataState.collectAsState().value
    val stepData = mainViewModel.stepsState.collectAsState().value

    MaterialTheme {
        Column {
            CollectorStatus(recentData?: RecentEntity(timestamp = -1, acc = "null", hr = "null",ppg= "null"))
            HorizontalDivider()
            StepStatus(
                stepData = stepData?: StepEntity(dataReceived = -1, startTime = -1, endTime = -1, step = 0)
            )
            HorizontalDivider()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick={ mainViewModel.flush() }
                ) {
                    Text("Flush collected data")
                }
            }

            EventRecorder(events = events) {
                mainViewModel.onClick()
            }
        }
    }
}

@Composable
fun CollectorStatus(recentData: RecentEntity) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CURRENT STATUS")
        Text("timestamp: ${recentData.timestamp}")
        Text("HR: ${recentData.hr}")
        Text("ACC: ${recentData.acc}")
        Text("PPG: ${recentData.ppg}")
    }
}

@Composable
fun EventRecorder(events: List<EventEntity>, onClick: () -> Unit) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onClick) {
                Text("RECORD EVENT")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            events.forEach {
                item {
                    Text("${it.id}: ${timestampToString(it.timestamp)}(${it.timestamp})")
                }
            }
        }
    }
}

@Composable
fun StepStatus(
    stepData: StepEntity,
    modifier: Modifier = Modifier
) {
    val startTime = timestampToString(stepData.startTime)
    val endTime = timestampToString(stepData.endTime)

    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick={
                val intent = Intent(context, CollectorService::class.java)
                ContextCompat.startForegroundService(context, intent)
                Log.d("MainApp", "start")
            }
        ) {
            Text("Start Step Tracking")
        }

        Text (
            "Last Step: start = ${startTime}, end = ${endTime}, steps = ${stepData.step}"
        )
    }
}

fun timestampToString(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}