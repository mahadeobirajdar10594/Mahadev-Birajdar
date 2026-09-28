package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DashboardTab
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge

data class PaletteAction(
    val title: String,
    val subtitle: String,
    val shortcut: String,
    val icon: ImageVector,
    val execute: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    viewModel: TaskPulseViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

    val actions = remember {
        listOf(
            PaletteAction(
                title = "Create New Sprint Task",
                subtitle = "Add a task with priority, tags, and assignee",
                shortcut = "N",
                icon = Icons.Default.Add,
                execute = {
                    onDismiss()
                    viewModel.openNewTaskDialog()
                }
            ),
            PaletteAction(
                title = "Switch to Kanban Board",
                subtitle = "Drag-and-drop workflow columns",
                shortcut = "B",
                icon = Icons.Default.ViewKanban,
                execute = {
                    onDismiss()
                    viewModel.setTab(DashboardTab.BOARD)
                }
            ),
            PaletteAction(
                title = "Switch to Task List",
                subtitle = "Sortable tabular overview",
                shortcut = "L",
                icon = Icons.Default.FormatListBulleted,
                execute = {
                    onDismiss()
                    viewModel.setTab(DashboardTab.LIST)
                }
            ),
            PaletteAction(
                title = "Open Performance Analytics",
                subtitle = "Velocity, burndown, throughput reports",
                shortcut = "A",
                icon = Icons.Default.Analytics,
                execute = {
                    onDismiss()
                    viewModel.setTab(DashboardTab.ANALYTICS)
                }
            ),
            PaletteAction(
                title = "Sync All Sprint Deadlines",
                subtitle = "Export tasks to Google Calendar",
                shortcut = "S",
                icon = Icons.Default.CalendarMonth,
                execute = {
                    onDismiss()
                    viewModel.syncAllSprintTasksToCalendar(context)
                }
            ),
            PaletteAction(
                title = "Toggle Dark Mode",
                subtitle = "Switch between dark and light themes",
                shortcut = "T",
                icon = Icons.Default.DarkMode,
                execute = {
                    viewModel.toggleDarkTheme()
                }
            ),
            PaletteAction(
                title = "View Keyboard Shortcuts",
                subtitle = "Cheatsheet of all productivity hotkeys",
                shortcut = "?",
                icon = Icons.Default.Keyboard,
                execute = {
                    onDismiss()
                    viewModel.openShortcutsDialog()
                }
            )
        )
    }

    val filteredActions = actions.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true)
    }

    val matchingTasks = if (query.isNotBlank()) {
        tasks.filter { it.title.contains(query, ignoreCase = true) || it.tags.contains(query, ignoreCase = true) }
    } else emptyList()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_command_palette")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Input with Ctrl+K header
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type a command or search tasks… (Ctrl+K)", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_command_query"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (matchingTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tasks Found (${matchingTasks.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(matchingTasks.take(4)) { task ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        viewModel.selectTaskDetail(task)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = task.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${task.assigneeName} • ${task.tags}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(status = task.status)
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }

                    item {
                        Text(
                            text = "Actions & Navigation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(filteredActions) { action ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { action.execute() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = action.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = action.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = action.shortcut,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
