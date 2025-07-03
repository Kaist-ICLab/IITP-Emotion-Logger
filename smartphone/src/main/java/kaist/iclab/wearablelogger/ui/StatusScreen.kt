package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.RecentEntity
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusScreen(
    statusViewModel: StatusViewModel,
    modifier: Modifier = Modifier
) {
    val recentData = statusViewModel.recentDataState.collectAsState().value
    val stepData = statusViewModel.stepsState.collectAsState().value
    val environmentData = statusViewModel.environmentState.collectAsState().value
    val syncTime = statusViewModel.syncTime.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        CollectorStatus(recentData?: RecentEntity(timestamp = -1, acc = "null", hr = "null", ppg= "null", skinTemp = "null"))
        HorizontalDivider()
//        StepStatus(
//            stepData = stepData?: StepEntity(dataReceived = -1, startTime = -1, endTime = -1, step = 0)
//        )
        SyncStatus(
            accSyncTime = syncTime[CollectorType.ACC] ?: -1,
            hrSyncTime = syncTime[CollectorType.HR] ?: -1,
            ppgSyncTime = syncTime[CollectorType.PPG] ?: -1,
            skinTempSyncTime = syncTime[CollectorType.SKINTEMP] ?: -1
        )
        HorizontalDivider()
//        EnvironmentStatus(
//            environmentData = environmentData?: EnvEntity(timestamp = -1, temperature = 0.0, humidity = 0.0, tvoc = 0, co2 = 0)
//        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick={ statusViewModel.flush() }
            ) {
                Text("Flush collected data")
            }
        }
    }
}

@Composable
fun CollectorStatus(recentData: RecentEntity) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "Last received: ",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(timestampToString(recentData.timestamp))
            }
        }

        Text("HR: ${recentData.hr}")
        Text("ACC: ${recentData.acc}")
        Text("PPG: ${recentData.ppg}")
        Text("SkinTemp: ${recentData.skinTemp}")
    }
}

@Composable
fun StepStatus(
    stepData: StepEntity,
    modifier: Modifier = Modifier
) {
    val startTime = timestampToString(stepData.startTime)
    val endTime = timestampToString(stepData.endTime)

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text (
            "Last Step: start = ${startTime}, end = ${endTime}, steps = ${stepData.step}"
        )
    }
}

@Composable
fun EnvironmentStatus(
    environmentData: EnvEntity,
    modifier: Modifier = Modifier
) {
    val timestamp = environmentData.timestamp
    val temperature = environmentData.temperature
    val humidity = environmentData.humidity
    val co2 = environmentData.co2
    val tvoc = environmentData.tvoc

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text (
            "Environment(${timestampToString(timestamp)}): temperature=${temperature}, humidity=$humidity, CO2=$co2, tVOC=$tvoc "
        )
    }
}

@Composable
fun SyncStatus(
    hrSyncTime: Long,
    accSyncTime: Long,
    ppgSyncTime: Long,
    skinTempSyncTime: Long,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Last Sync Time",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        SyncTimeCard(
            sensorName = "HR",
            time = hrSyncTime
        )
        SyncTimeCard(
            sensorName = "ACC",
            time = accSyncTime
        )
        SyncTimeCard(
            sensorName = "PPG",
            time = ppgSyncTime
        )
        SyncTimeCard(
            sensorName = "SKINTEMP",
            time = skinTempSyncTime
        )
    }
}

@Composable
fun SyncTimeCard(
    sensorName: String,
    time: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("$sensorName: ${timestampToString(time)}")
        }
    }
}

@Composable
@Preview
fun SyncStatusPreview() {
    MaterialTheme {
        SyncStatus(
            hrSyncTime = 0,
            ppgSyncTime = 0,
            accSyncTime = 0,
            skinTempSyncTime = 0
        )
    }
}

fun timestampToString(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}