package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.RecentEntity
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

    Column(
        modifier = modifier.padding(8.dp)
    ) {
        CollectorStatus(recentData?: RecentEntity(timestamp = -1, acc = "null", hr = "null", ppg= "null", skinTemp = "null"))
        HorizontalDivider()
        StepStatus(
            stepData = stepData?: StepEntity(dataReceived = -1, startTime = -1, endTime = -1, step = 0)
        )
        HorizontalDivider()
        EnvironmentStatus(
            environmentData = environmentData?: EnvEntity(timestamp = -1, temperature = 0.0, humidity = 0.0, tvoc = 0, co2 = 0)
        )
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CURRENT STATUS")
        Text("timestamp: ${recentData.timestamp}")
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

fun timestampToString(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}