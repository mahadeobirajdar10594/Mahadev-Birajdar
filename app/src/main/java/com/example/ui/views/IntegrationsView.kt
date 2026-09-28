package com.example.ui.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.IntegrationsConfig
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.RoleBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IntegrationsView(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.integrationsConfig.collectAsStateWithLifecycle()
    val activeRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

    // Local form states
    var webhookUrl by remember(config) { mutableStateOf(config.slackWebhookUrl) }
    var channel by remember(config) { mutableStateOf(config.slackChannel) }
    var isSlackEnabled by remember(config) { mutableStateOf(config.isSlackEnabled) }
    var notifyOnCreate by remember(config) { mutableStateOf(config.notifyOnCreate) }
    var notifyOnStatusChange by remember(config) { mutableStateOf(config.notifyOnStatusChange) }
    var notifyOnDone by remember(config) { mutableStateOf(config.notifyOnDone) }
    var isCalendarEnabled by remember(config) { mutableStateOf(config.isGoogleCalendarEnabled) }
    var autoSyncDueDates by remember(config) { mutableStateOf(config.autoSyncDueDates) }

    val canManage = activeRole.canManageIntegrations

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("integrations_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Role Notice
        if (!canManage) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viewing as ${activeRole.displayName}. Only Admin can modify integration webhooks.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Google Calendar Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Google Calendar",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Calendar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automated Deadline & Event Sync",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isCalendarEnabled,
                        onCheckedChange = { isCalendarEnabled = it },
                        enabled = canManage
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Auto-sync setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-sync task due dates",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add tasks to device calendar on due date update",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = autoSyncDueDates,
                        onCheckedChange = { autoSyncDueDates = it },
                        enabled = canManage
                    )
                }

                if (config.lastCalendarSyncTime != null) {
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.US).format(Date(config.lastCalendarSyncTime!!))
                    Text(
                        text = "Last synced: $dateFormatted",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.syncAllSprintTasksToCalendar(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_sync_google_calendar"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Sprint Deadlines to Google Calendar")
                }
            }
        }

        // Slack Integration Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE01E5A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Slack",
                                tint = Color(0xFFE01E5A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Slack Integration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Live Activity & Channel Notifications",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isSlackEnabled,
                        onCheckedChange = { isSlackEnabled = it },
                        enabled = canManage
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Webhook URL
                Text(
                    text = "Incoming Webhook URL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    placeholder = { Text("https://hooks.slack.com/services/...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_slack_webhook"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = canManage,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Channel Name
                Text(
                    text = "Alerts Channel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = channel,
                    onValueChange = { channel = it },
                    placeholder = { Text("#proj-taskpulse") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_slack_channel"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = canManage,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Automated Event Triggers",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = notifyOnCreate,
                        onCheckedChange = { notifyOnCreate = it },
                        enabled = canManage
                    )
                    Text(
                        text = "Notify channel when a new task is created",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = notifyOnStatusChange,
                        onCheckedChange = { notifyOnStatusChange = it },
                        enabled = canManage
                    )
                    Text(
                        text = "Notify on task status changes (moved across columns)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = notifyOnDone,
                        onCheckedChange = { notifyOnDone = it },
                        enabled = canManage
                    )
                    Text(
                        text = "Broadcast celebratory alert when tasks hit Done",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Last test status
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Test Status: ${config.lastSlackTestStatus}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val updated = config.copy(
                                slackWebhookUrl = webhookUrl,
                                slackChannel = channel,
                                isSlackEnabled = isSlackEnabled,
                                notifyOnCreate = notifyOnCreate,
                                notifyOnStatusChange = notifyOnStatusChange,
                                notifyOnDone = notifyOnDone,
                                isGoogleCalendarEnabled = isCalendarEnabled,
                                autoSyncDueDates = autoSyncDueDates
                            )
                            viewModel.sendTestSlackAlert(updated)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_slack_webhook"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Test",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Alert", fontSize = 12.sp)
                    }

                    if (canManage) {
                        Button(
                            onClick = {
                                val updated = config.copy(
                                    slackWebhookUrl = webhookUrl,
                                    slackChannel = channel,
                                    isSlackEnabled = isSlackEnabled,
                                    notifyOnCreate = notifyOnCreate,
                                    notifyOnStatusChange = notifyOnStatusChange,
                                    notifyOnDone = notifyOnDone,
                                    isGoogleCalendarEnabled = isCalendarEnabled,
                                    autoSyncDueDates = autoSyncDueDates
                                )
                                viewModel.saveIntegrations(updated)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_integrations"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Config", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
