package kaist.iclab.wearablelogger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kaist.iclab.loggerstructure.util.TimeUtil

@Composable
fun Accordion(
    imageVector: ImageVector,
    title: String,
    syncTime: Long?,
    uploadTime: Long,
    isExpanded: Boolean,
    toggleExpansion: () -> Unit,
    button: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = toggleExpansion)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1F)
            ) {
                // Title
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    button()
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Upload / Sync time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Uploaded",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = TimeUtil.timestampToString(uploadTime),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    if(syncTime != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Synced",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = TimeUtil.timestampToString(syncTime),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )

        }

        // Expandable Content
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                content() // 하위 Composable을 여기에 렌더링
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun AccordionPreview() {
    val currentTime = System.currentTimeMillis()
    MaterialTheme {
        Accordion(
            imageVector = Icons.Default.Favorite,
            title = "Meow",
            syncTime = 17517226310000,
            uploadTime = currentTime,
            isExpanded = true,
            toggleExpansion = {},
        ) {
            Text("Hello")
        }
    }
}