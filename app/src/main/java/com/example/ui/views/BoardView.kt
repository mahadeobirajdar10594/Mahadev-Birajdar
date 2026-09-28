package com.example.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.PresenceStatus
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.TaskStatus
import com.example.model.TeamMember
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoardView(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val members by viewModel.teamMembers.collectAsStateWithLifecycle()
    val draggedTask by viewModel.draggedTask.collectAsStateWithLifecycle()
    val dragTargetColumn by viewModel.dragTargetColumn.collectAsStateWithLifecycle()
    val activeRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.filterPriority.collectAsStateWithLifecycle()

    // Store global coordinates of columns for drop targeting
    val columnBounds = remember { mutableMapOf<TaskStatus, Pair<Float, Float>>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("board_view")
    ) {
        // Hero Header Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(130.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_dashboard_hero),
                    contentDescription = "Dashboard Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xEE090D16),
                                    Color(0xCC0F172A),
                                    Color(0x881E1B4B)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sprint Active • Real-Time Board",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Productivity Dashboard",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Active avatars row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Collaborators:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            members.take(4).forEach { member ->
                                UserAvatar(
                                    name = member.name,
                                    avatarColorHex = member.avatarColorHex,
                                    presence = member.presence,
                                    sizeDp = 24,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }

                    if (activeRole.canCreateTask) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.openNewTaskDialog() }
                                .testTag("btn_quick_add_hero")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Task",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Priority Filter Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedPriority == null,
                onClick = { viewModel.filterPriority.value = null },
                label = { Text("All Tasks (${tasks.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors()
            )
            TaskPriority.values().forEach { prio ->
                val count = tasks.count { it.priority == prio }
                FilterChip(
                    selected = selectedPriority == prio,
                    onClick = {
                        viewModel.filterPriority.value = if (selectedPriority == prio) null else prio
                    },
                    label = { Text("${prio.displayName} ($count)", fontSize = 12.sp) }
                )
            }
        }

        // Kanban Board Columns (Horizontal Scrolling)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskStatus.values().forEach { status ->
                val columnTasks = tasks.filter { it.status == status }
                val isTarget = dragTargetColumn == status

                KanbanColumn(
                    status = status,
                    tasks = columnTasks,
                    isTarget = isTarget,
                    draggedTask = draggedTask,
                    canEdit = activeRole.canEditTask,
                    onTaskClick = { viewModel.selectTaskDetail(it) },
                    onStartDrag = { viewModel.startDrag(it) },
                    onDragMove = { offset ->
                        // Detect column based on horizontal bounds
                        val target = columnBounds.entries.find { (_, bounds) ->
                            offset.x >= bounds.first && offset.x <= bounds.second
                        }?.key
                        viewModel.setHoverColumn(target)
                    },
                    onEndDrag = {
                        viewModel.endDrag(dragTargetColumn)
                    },
                    onQuickMove = { task, newStatus ->
                        viewModel.moveTaskStatus(task.id, newStatus)
                    },
                    onGloballyPositioned = { minX, maxX ->
                        columnBounds[status] = Pair(minX, maxX)
                    },
                    onAddTask = {
                        viewModel.openNewTaskDialog()
                    },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
fun KanbanColumn(
    status: TaskStatus,
    tasks: List<Task>,
    isTarget: Boolean,
    draggedTask: Task?,
    canEdit: Boolean,
    onTaskClick: (Task) -> Unit,
    onStartDrag: (Task) -> Unit,
    onDragMove: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onQuickMove: (Task, TaskStatus) -> Unit,
    onGloballyPositioned: (Float, Float) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isTarget) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "column_border"
    )

    Card(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInWindow()
                onGloballyPositioned(pos.x, pos.x + coordinates.size.width)
            }
            .border(
                width = if (isTarget) 2.dp else 1.dp,
                color = if (isTarget) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTarget) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Column Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = status)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${tasks.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (canEdit && status == TaskStatus.TODO) {
                    IconButton(
                        onClick = onAddTask,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task to Column",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Task Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    val isBeingDragged = draggedTask?.id == task.id
                    KanbanTaskCard(
                        task = task,
                        isBeingDragged = isBeingDragged,
                        canEdit = canEdit,
                        onClick = { onTaskClick(task) },
                        onStartDrag = { onStartDrag(task) },
                        onDragMove = onDragMove,
                        onEndDrag = onEndDrag,
                        onQuickMove = { newStatus -> onQuickMove(task, newStatus) }
                    )
                }

                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Drop tasks here",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanTaskCard(
    task: Task,
    isBeingDragged: Boolean,
    canEdit: Boolean,
    onClick: () -> Unit,
    onStartDrag: () -> Unit,
    onDragMove: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onQuickMove: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (isBeingDragged) 1.04f else 1f, label = "card_scale")
    val rotation by animateFloatAsState(targetValue = if (isBeingDragged) 2f else 0f, label = "card_rot")
    val elevation = if (isBeingDragged) 12.dp else 2.dp

    var dragAccumulator by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .rotate(rotation)
            .shadow(elevation, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (canEdit) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                dragAccumulator = offset
                                onStartDrag()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulator += dragAmount
                                onDragMove(dragAccumulator)
                            },
                            onDragEnd = { onEndDrag() },
                            onDragCancel = { onEndDrag() }
                        )
                    }
                } else Modifier
            )
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBeingDragged) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Priority & Drag indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PriorityBadge(priority = task.priority)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.estimatedHours > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Hours",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${task.spentHours}/${task.estimatedHours}h",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (canEdit) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Hold to drag",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = task.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Tags
            if (task.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    task.tags.split(",").take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#${tag.trim()}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Assignee & Due Date & Quick Move
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        name = task.assigneeName,
                        presence = PresenceStatus.ONLINE,
                        sizeDp = 22
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.assigneeName.split(" ").firstOrNull() ?: "Team",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.dueDate != null) {
                    val isOverdue = task.dueDate < System.currentTimeMillis() && task.status != TaskStatus.DONE
                    val dateStr = SimpleDateFormat("MMM d", Locale.US).format(Date(task.dueDate))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Due Date",
                            tint = if (isOverdue) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                            color = if (isOverdue) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Move Bar (Accessible 1-tap column shift)
            if (canEdit) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    when (task.status) {
                        TaskStatus.TODO -> {
                            QuickMoveChip(label = "Start →", target = TaskStatus.IN_PROGRESS, onClick = onQuickMove)
                        }
                        TaskStatus.IN_PROGRESS -> {
                            QuickMoveChip(label = "Review →", target = TaskStatus.IN_REVIEW, onClick = onQuickMove)
                        }
                        TaskStatus.IN_REVIEW -> {
                            QuickMoveChip(label = "Done ✓", target = TaskStatus.DONE, onClick = onQuickMove)
                        }
                        TaskStatus.DONE -> {
                            QuickMoveChip(label = "Reopen", target = TaskStatus.IN_PROGRESS, onClick = onQuickMove)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMoveChip(
    label: String,
    target: TaskStatus,
    onClick: (TaskStatus) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.clickable { onClick(target) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
