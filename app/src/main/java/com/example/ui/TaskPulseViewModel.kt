package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.model.ActivityNotification
import com.example.model.IntegrationsConfig
import com.example.model.Task
import com.example.model.TaskComment
import com.example.model.TaskPriority
import com.example.model.TaskStatus
import com.example.model.TeamMember
import com.example.model.UserRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DashboardTab(val title: String) {
    BOARD("Board"),
    LIST("List"),
    ANALYTICS("Analytics"),
    ACTIVITY("Activity"),
    INTEGRATIONS("Integrations"),
    TEAM("Team")
}

data class AnalyticsData(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val reviewCount: Int = 0,
    val todoCount: Int = 0,
    val completionPercentage: Int = 0,
    val totalEstimatedHours: Int = 0,
    val totalSpentHours: Int = 0,
    val urgentCount: Int = 0,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0,
    val overdueCount: Int = 0,
    val memberStats: List<Pair<String, Int>> = emptyList()
)

class TaskPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = TaskRepository(database)

    // Current logged-in user profile
    val currentUserName = MutableStateFlow("Alex Chen")
    val currentUserRole = MutableStateFlow(UserRole.ADMIN)

    // Dark Mode state (defaults to true for a sleek modern dashboard)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(DashboardTab.BOARD)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    // Dialog & Sheet States
    private val _showNewTaskDialog = MutableStateFlow(false)
    val showNewTaskDialog: StateFlow<Boolean> = _showNewTaskDialog.asStateFlow()

    private val _showCommandPalette = MutableStateFlow(false)
    val showCommandPalette: StateFlow<Boolean> = _showCommandPalette.asStateFlow()

    private val _showShortcutsDialog = MutableStateFlow(false)
    val showShortcutsDialog: StateFlow<Boolean> = _showShortcutsDialog.asStateFlow()

    private val _showRolePicker = MutableStateFlow(false)
    val showRolePicker: StateFlow<Boolean> = _showRolePicker.asStateFlow()

    private val _selectedTaskForDetail = MutableStateFlow<Task?>(null)
    val selectedTaskForDetail: StateFlow<Task?> = _selectedTaskForDetail.asStateFlow()

    // Live Collaboration Simulation
    private val _isLiveCollabActive = MutableStateFlow(true)
    val isLiveCollabActive: StateFlow<Boolean> = _isLiveCollabActive.asStateFlow()

    private val _liveToastMessage = MutableStateFlow<String?>(null)
    val liveToastMessage: StateFlow<String?> = _liveToastMessage.asStateFlow()

    // Drag-and-drop state
    private val _draggedTask = MutableStateFlow<Task?>(null)
    val draggedTask: StateFlow<Task?> = _draggedTask.asStateFlow()

    private val _dragTargetColumn = MutableStateFlow<TaskStatus?>(null)
    val dragTargetColumn: StateFlow<TaskStatus?> = _dragTargetColumn.asStateFlow()

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val filterPriority = MutableStateFlow<TaskPriority?>(null)
    val filterStatus = MutableStateFlow<TaskStatus?>(null)
    val filterAssignee = MutableStateFlow<Long?>(null)

    // Data streams from repository
    val allTasks: StateFlow<List<Task>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMembers: StateFlow<List<TeamMember>> = repository.membersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<ActivityNotification>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val integrationsConfig: StateFlow<IntegrationsConfig> = repository.integrationsConfigFlow
        .combine(MutableStateFlow(IntegrationsConfig())) { config, fallback ->
            config ?: fallback
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IntegrationsConfig())

    // Filtered tasks
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        searchQuery,
        filterPriority,
        filterStatus,
        filterAssignee
    ) { tasks, query, priority, status, assigneeId ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.tags.contains(query, ignoreCase = true) ||
                    task.assigneeName.contains(query, ignoreCase = true)

            val matchesPriority = priority == null || task.priority == priority
            val matchesStatus = status == null || task.status == status
            val matchesAssignee = assigneeId == null || task.assigneeId == assigneeId

            matchesQuery && matchesPriority && matchesStatus && matchesAssignee
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics computation
    val analytics: StateFlow<AnalyticsData> = allTasks.combine(teamMembers) { tasks, members ->
        val total = tasks.size
        val done = tasks.count { it.status == TaskStatus.DONE }
        val inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val review = tasks.count { it.status == TaskStatus.IN_REVIEW }
        val todo = tasks.count { it.status == TaskStatus.TODO }
        val pct = if (total > 0) ((done.toFloat() / total) * 100).toInt() else 0

        val estHours = tasks.sumOf { it.estimatedHours }
        val spentHours = tasks.sumOf { it.spentHours }

        val urgent = tasks.count { it.priority == TaskPriority.URGENT }
        val high = tasks.count { it.priority == TaskPriority.HIGH }
        val medium = tasks.count { it.priority == TaskPriority.MEDIUM }
        val low = tasks.count { it.priority == TaskPriority.LOW }

        val now = System.currentTimeMillis()
        val overdue = tasks.count { it.dueDate != null && it.dueDate < now && it.status != TaskStatus.DONE }

        val memberStats = members.map { member ->
            val memberDone = tasks.count { it.assigneeId == member.id && it.status == TaskStatus.DONE }
            Pair(member.name, memberDone)
        }

        AnalyticsData(
            totalCount = total,
            completedCount = done,
            inProgressCount = inProgress,
            reviewCount = review,
            todoCount = todo,
            completionPercentage = pct,
            totalEstimatedHours = estHours,
            totalSpentHours = spentHours,
            urgentCount = urgent,
            highCount = high,
            mediumCount = medium,
            lowCount = low,
            overdueCount = overdue,
            memberStats = memberStats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsData())

    private var liveSimulationJob: Job? = null

    init {
        // Start live collaboration periodic event simulator
        startLiveCollaborationSimulation()
    }

    private fun startLiveCollaborationSimulation() {
        liveSimulationJob?.cancel()
        liveSimulationJob = viewModelScope.launch {
            while (true) {
                delay(22_000) // every 22 seconds emit a team event
                if (_isLiveCollabActive.value) {
                    val alert = repository.triggerSimulatedCollaborationEvent()
                    _liveToastMessage.value = alert
                    delay(4000)
                    _liveToastMessage.value = null
                }
            }
        }
    }

    fun toggleLiveCollaboration() {
        _isLiveCollabActive.value = !_isLiveCollabActive.value
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    fun openNewTaskDialog() {
        if (!currentUserRole.value.canCreateTask) {
            Toast.makeText(getApplication(), "Viewers cannot create tasks", Toast.LENGTH_SHORT).show()
            return
        }
        _showNewTaskDialog.value = true
    }

    fun closeNewTaskDialog() {
        _showNewTaskDialog.value = false
    }

    fun openCommandPalette() {
        _showCommandPalette.value = true
    }

    fun closeCommandPalette() {
        _showCommandPalette.value = false
    }

    fun openShortcutsDialog() {
        _showShortcutsDialog.value = true
    }

    fun closeShortcutsDialog() {
        _showShortcutsDialog.value = false
    }

    fun openRolePicker() {
        _showRolePicker.value = true
    }

    fun closeRolePicker() {
        _showRolePicker.value = false
    }

    fun selectTaskDetail(task: Task?) {
        _selectedTaskForDetail.value = task
    }

    fun switchActiveUserRole(role: UserRole) {
        currentUserRole.value = role
        _showRolePicker.value = false
        Toast.makeText(getApplication(), "Active user role changed to: ${role.displayName}", Toast.LENGTH_SHORT).show()
    }

    // Task CRUD
    fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
        assigneeId: Long?,
        assigneeName: String,
        dueDate: Long?,
        tags: String,
        estimatedHours: Int
    ) {
        if (!currentUserRole.value.canCreateTask) {
            Toast.makeText(getApplication(), "Permission denied: Viewer cannot create tasks", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    priority = priority,
                    assigneeId = assigneeId,
                    assigneeName = assigneeName,
                    dueDate = dueDate,
                    tags = tags,
                    estimatedHours = estimatedHours,
                    status = TaskStatus.TODO
                ),
                creatorName = currentUserName.value
            )
            _showNewTaskDialog.value = false
        }
    }

    fun updateTask(task: Task) {
        if (!currentUserRole.value.canEditTask) {
            Toast.makeText(getApplication(), "Permission denied: Viewer role cannot edit tasks", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.updateTask(task, actorName = currentUserName.value)
            _selectedTaskForDetail.value = task
        }
    }

    fun moveTaskStatus(taskId: Long, newStatus: TaskStatus) {
        if (!currentUserRole.value.canEditTask) {
            Toast.makeText(getApplication(), "Permission denied: Viewer role cannot change status", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, newStatus, actorName = currentUserName.value)
        }
    }

    fun deleteTask(task: Task) {
        if (!currentUserRole.value.canDeleteTask) {
            Toast.makeText(getApplication(), "Permission denied: Only Admin or Manager can delete tasks", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.deleteTask(task, actorName = currentUserName.value)
            _selectedTaskForDetail.value = null
        }
    }

    fun addComment(taskId: Long, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.addComment(
                taskId = taskId,
                authorName = currentUserName.value,
                authorRole = currentUserRole.value,
                message = message
            )
        }
    }

    // Drag and Drop
    fun startDrag(task: Task) {
        if (!currentUserRole.value.canEditTask) return
        _draggedTask.value = task
    }

    fun setHoverColumn(status: TaskStatus?) {
        _dragTargetColumn.value = status
    }

    fun endDrag(droppedColumn: TaskStatus?) {
        val task = _draggedTask.value
        if (task != null && droppedColumn != null && task.status != droppedColumn) {
            moveTaskStatus(task.id, droppedColumn)
        }
        _draggedTask.value = null
        _dragTargetColumn.value = null
    }

    // Google Calendar Sync
    fun syncTaskToCalendar(context: Context, task: Task) {
        try {
            val intent = repository.createGoogleCalendarIntent(task)
            context.startActivity(intent)
            viewModelScope.launch {
                repository.recordCalendarSync(1, currentUserName.value)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Calendar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun syncAllSprintTasksToCalendar(context: Context) {
        val tasksWithDates = allTasks.value.filter { it.dueDate != null && it.status != TaskStatus.DONE }
        if (tasksWithDates.isEmpty()) {
            Toast.makeText(context, "No pending tasks with due dates to sync", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.recordCalendarSync(tasksWithDates.size, currentUserName.value)
        }
        // Launch first upcoming task in Calendar
        syncTaskToCalendar(context, tasksWithDates.first())
        Toast.makeText(context, "Synced ${tasksWithDates.size} sprint tasks with Google Calendar", Toast.LENGTH_LONG).show()
    }

    // Slack Integration
    fun saveIntegrations(config: IntegrationsConfig) {
        if (!currentUserRole.value.canManageIntegrations) {
            Toast.makeText(getApplication(), "Permission denied: Only Admin can manage integrations", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.saveIntegrationsConfig(config)
            Toast.makeText(getApplication(), "Integration settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendTestSlackAlert(config: IntegrationsConfig) {
        viewModelScope.launch {
            val result = repository.sendSlackTest(config)
            if (result.first) {
                Toast.makeText(getApplication(), "Slack notification sent successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(getApplication(), "Slack webhook result: ${result.second}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Notifications
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    fun triggerCollaborationEvent() {
        viewModelScope.launch {
            val alert = repository.triggerSimulatedCollaborationEvent()
            _liveToastMessage.value = alert
            delay(4000)
            _liveToastMessage.value = null
        }
    }

    fun updateMemberRole(memberId: Long, newRole: UserRole) {
        if (!currentUserRole.value.canAssignRoles) {
            Toast.makeText(getApplication(), "Permission denied: Only Admin can assign roles", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.updateMemberRole(memberId, newRole, currentUserName.value)
        }
    }

    // Keyboard Shortcuts Executor
    fun handleShortcutKey(key: String, context: Context): Boolean {
        when (key.uppercase()) {
            "N" -> {
                openNewTaskDialog()
                return true
            }
            "B" -> {
                setTab(DashboardTab.BOARD)
                return true
            }
            "L" -> {
                setTab(DashboardTab.LIST)
                return true
            }
            "A" -> {
                setTab(DashboardTab.ANALYTICS)
                return true
            }
            "I" -> {
                setTab(DashboardTab.INTEGRATIONS)
                return true
            }
            "T" -> {
                toggleDarkTheme()
                return true
            }
            "K" -> {
                openCommandPalette()
                return true
            }
            "?" -> {
                openShortcutsDialog()
                return true
            }
            "R" -> {
                viewModelScope.launch {
                    repository.triggerSimulatedCollaborationEvent()
                }
                Toast.makeText(context, "Sprint board synced", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }
}
