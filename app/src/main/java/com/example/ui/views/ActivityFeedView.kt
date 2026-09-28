package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ActivityNotification
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.UserAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityFeedView(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLiveActive by viewModel.isLiveCollabActive.collectAsStateWithLifecycle()
    var filterUnreadOnly by remember { mutableStateOf(false) }

    val displayedList = if (filterUnreadOnly) notifications.filter { !it.isRead } else notifications

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("activity_feed_view")
    ) {
        // Live Simulation Banner & Controls
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isLiveActive) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Live Activity Stream",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isLiveActive) "Listening for real-time team events" else "Stream paused",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isLiveActive,
                        onCheckedChange = { viewModel.toggleLiveCollaboration() },
                        modifier = Modifier.testTag("toggle_live_collab")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerCollaborationEvent()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_trigger_event"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simulate",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger Live Event", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.markAllNotificationsRead() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_mark_read")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Read", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Feed Filter & Count Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterUnreadOnly,
                    onClick = { filterUnreadOnly = false },
                    label = { Text("All (${notifications.size})", fontSize = 12.sp) }
                )
                val unreadCount = notifications.count { !it.isRead }
                FilterChip(
                    selected = filterUnreadOnly,
                    onClick = { filterUnreadOnly = true },
                    label = { Text("Unread ($unreadCount)", fontSize = 12.sp) }
                )
            }

            if (notifications.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllNotifications() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Notifications List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedList, key = { it.id }) { item ->
                NotificationItemCard(notification = item)
            }

            if (displayedList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterUnreadOnly) "No unread notifications" else "Activity feed is clean",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: ActivityNotification,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = when (notification.actionType) {
        "CREATED" -> Pair(Icons.Default.AddCircle, MaterialTheme.colorScheme.primary)
        "COMPLETED" -> Pair(Icons.Default.CheckCircle, Color(0xFF10B981))
        "COMMENTED" -> Pair(Icons.Default.Chat, Color(0xFF8B5CF6))
        "SYNCED" -> Pair(Icons.Default.CalendarToday, Color(0xFF4285F4))
        "SLACK_ALERT_SENT" -> Pair(Icons.Default.NotificationsActive, Color(0xFFE01E5A))
        else -> Pair(Icons.Default.SyncAlt, Color(0xFF06B6D4))
    }

    val relativeTime = formatRelativeTime(notification.timestamp)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (notification.isRead) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.actorName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = relativeTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.US).format(Date(timestamp))
    }
}
