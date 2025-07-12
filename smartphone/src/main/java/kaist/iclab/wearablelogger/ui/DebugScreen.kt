package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DebugScreen(
    uploadSingleStepEntity: () -> Unit,
    flush: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = uploadSingleStepEntity,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send dummy Step entity")
        }

        Button(
            onClick = flush,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Flush all data")
        }
    }
}

@Composable
@Preview(
    showBackground = true
)
fun DebugScreenPreview() {
    MaterialTheme {
        DebugScreen(
            uploadSingleStepEntity = {},
            flush = {}
        )
    }
}