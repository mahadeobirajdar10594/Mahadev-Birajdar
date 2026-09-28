package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TaskPriority
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.PriorityBadge
import com.example.ui.components.UserAvatar

@Composable
fun NewTaskDialog(
    viewModel: TaskPulseViewModel,
    onDismiss: () -> Unit
) {
    val members by viewModel.teamMembers.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedAssignee by remember { mutableStateOf(members.firstOrNull()) }
    var tags by remember { mutableStateOf("Feature") }
    var estimatedHours by remember { mutableStateOf("4") }

    // Due date presets (days from now)
    var selectedDaysOffset by remember { mutableStateOf(3) }

    var priorityExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_new_task")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New Sprint Task",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Implement Webhook Dispatcher") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Acceptance Criteria") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("input_task_description"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Priority & Assignee Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedPriority.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = {
                                IconButton(onClick = { priorityExpanded = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { priorityExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            TaskPriority.values().forEach { prio ->
                                DropdownMenuItem(
                                    text = { PriorityBadge(priority = prio) },
                                    onClick = {
                                        selectedPriority = prio
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Assignee Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedAssignee?.name ?: "Assignee",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assignee") },
                            trailingIcon = {
                                IconButton(onClick = { assigneeExpanded = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { assigneeExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = assigneeExpanded,
                            onDismissRequest = { assigneeExpanded = false }
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            UserAvatar(name = member.name, avatarColorHex = member.avatarColorHex, sizeDp = 22)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(member.name, fontSize = 13.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedAssignee = member
                                        assigneeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date Presets
                Text(
                    text = "Deadline (Days from now):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "Tomorrow", 3 to "+3 Days", 5 to "+5 Days", 7 to "1 Week").forEach { (offset, label) ->
                        FilterChip(
                            selected = selectedDaysOffset == offset,
                            onClick = { selectedDaysOffset = offset },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tags & Est Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.weight(1.4f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = estimatedHours,
                        onValueChange = { estimatedHours = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Est. Hours") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dueMillis = System.currentTimeMillis() + (selectedDaysOffset * 86_400_000L)
                                val estHoursInt = estimatedHours.toIntOrNull() ?: 4
                                viewModel.createTask(
                                    title = title,
                                    description = description,
                                    priority = selectedPriority,
                                    assigneeId = selectedAssignee?.id,
                                    assigneeName = selectedAssignee?.name ?: "Alex Chen",
                                    dueDate = dueMillis,
                                    tags = tags,
                                    estimatedHours = estHoursInt
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_task")
                    ) {
                        Text("Create Task")
                    }
                }
            }
        }
    }
}
