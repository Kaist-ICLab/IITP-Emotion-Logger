package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import kaist.iclab.loggerstructure.util.TimeUtil
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    val isCollecting = settingsViewModel.isCollectorState.collectAsState().value
    val pid = settingsViewModel.pidState.collectAsState().value
    val label = settingsViewModel.labelState.collectAsState().value
    val record = settingsViewModel.recordState.collectAsState().value

    val listState = rememberScalingLazyListState() // for Scaling Lazy column

    //UI
    Scaffold(
        timeText = {
            TimeText(modifier = Modifier.scrollAway(listState))
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp),
        ) {
            SettingController(
                flush = { settingsViewModel.flush() },
                startLogging = {
                    settingsViewModel.startLogging() },
                stopLogging = {
                    settingsViewModel.stopLogging() },
                updatePid = { settingsViewModel.updatePid(it) },
                updateLabel = { settingsViewModel.updateLabel(it) },
                isCollecting = isCollecting,
                pid = pid,
                label = label,
            )
            ScalingLazyColumn(
                state = listState,
            ) { // Lazy column for WearOS
                items(record, key = { it.id }) {
                    RecordInfo(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        label = it.label,
                        onDelete = { settingsViewModel.deleteRecord(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingController(
    flush: () -> Unit,
    startLogging: () -> Unit,
    stopLogging: () -> Unit,
    updatePid: (String) -> Unit,
    updateLabel: (String) -> Unit,
    isCollecting: Boolean,
    pid: String,
    label: String,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                icon = if (isCollecting) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                onClick = if (isCollecting) stopLogging else startLogging,
                contentDescription = "Start/Stop Collection",
                backgroundColor = if (isCollecting) MaterialTheme.colors.error else MaterialTheme.colors.primary,
                buttonSize = 48.dp,
                iconSize = 36.dp,
            )

            IconButton(
                icon = Icons.Rounded.Delete,
                onClick = flush,
                contentDescription = "Delete all Data",
                backgroundColor = MaterialTheme.colors.secondary,
                buttonSize = 36.dp,
                iconSize = 24.dp,
            )
        }

        Row {
            Chip(
                label = { Text("PID: $pid") },
                onClick = { showNameDialog = true },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    .height(32.dp),
            )

            if (showNameDialog) {
                DropdownSelectDialog(
                    options = listOf("오은", "신재윤", "김지환", "김민기"),
                    onSelect = updatePid,
                    onDismiss = { showNameDialog = false }
                )
            }

            Log.d("main", label)
            Chip(
                label = { Text(label) },
                onClick = { showLabelDialog = true },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    .height(32.dp),
            )

            if (showLabelDialog) {
                DropdownSelectDialog(
                    options = listOf("A", "B", "C", "D", "E", "F", "G", "H"),
                    onSelect = updateLabel,
                    onDismiss = { showLabelDialog = false }
                )
            }
        }
    }

}

@Composable
fun DropdownSelectDialog(
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    Dialog(onDismissRequest = onDismiss) {
        Surface (
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colors.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(
                        scrollState
                    )
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    backgroundColor: Color,
    buttonSize: Dp = 32.dp,
    iconSize: Dp = 20.dp,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        modifier = Modifier
            .padding(4.dp)
            .size(buttonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun RecordInfo(
    startTime: Long,
    endTime: Long,
    label: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colors.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onDelete()
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                Text(
                    TimeUtil.timestampToString(startTime),
                    fontSize = MaterialTheme.typography.caption1.fontSize
                )
                Text(
                    TimeUtil.timestampToString(endTime),
                    fontSize = MaterialTheme.typography.caption1.fontSize
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = MaterialTheme.typography.display1.fontSize)
        }
    }
}

@Preview
@Composable
fun IconButtonPreview(){
    IconButton(
        icon = Icons.Default.PlayArrow,
        onClick = {},
        contentDescription =  "ASDAS",
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Preview(
    showBackground = true
)
@Composable
fun SettingControllerPreview(){
    MaterialTheme {
        SettingController(
            flush = {},
            startLogging = {},
            stopLogging = {},
            updatePid = {},
            updateLabel = {},
            isCollecting = false,
            pid = "이준모",
            label = "A",
        )
    }
}

@Preview
@Composable
fun RecordInfoPreview() {
    MaterialTheme {
        RecordInfo(
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 100000,
            label = "A",
            onDelete = {}
        )
    }
}

