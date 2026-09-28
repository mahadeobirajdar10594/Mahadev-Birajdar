package com.example.data

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.model.ActivityNotification
import com.example.model.IntegrationsConfig
import com.example.model.Task
import com.example.model.TaskComment
import com.example.model.TaskStatus
import com.example.model.TeamMember
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class TaskRepository(
    private val database: AppDatabase
) {
    private val taskDao = database.taskDao()
    private val teamMemberDao = database.teamMemberDao()
    private val taskCommentDao = database.taskCommentDao()
    private val activityDao = database.activityNotificationDao()
    private val integrationsDao = database.integrationsDao()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val tasksFlow: Flow<List<Task>> = taskDao.getAllTasksFlow()
    val membersFlow: Flow<List<TeamMember>> = teamMemberDao.getAllMembersFlow()
    val notificationsFlow: Flow<List<ActivityNotification>> = activityDao.getAllNotificationsFlow()
    val integrationsConfigFlow: Flow<IntegrationsConfig?> = integrationsDao.getConfigFlow()

    fun getCommentsForTaskFlow(taskId: Long): Flow<List<TaskComment>> =
        taskCommentDao.getCommentsForTaskFlow(taskId)

    suspend fun insertTask(task: Task, creatorName: String): Long = withContext(Dispatchers.IO) {
        val id = taskDao.insertTask(task)
        val createdTask = task.copy(id = id)

        // Log notification
        activityDao.insertNotification(
            ActivityNotification(
                taskId = id,
                actorName = creatorName,
                actionType = "CREATED",
                description = "created task '${task.title}' [${task.priority.displayName}]",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )

        // Check if Slack automated update enabled
        val config = integrationsDao.getConfig()
        if (config != null && config.isSlackEnabled && config.notifyOnCreate && config.slackWebhookUrl.isNotBlank()) {
            val slackMsg = ":sparkles: *New Task Created* by $creatorName: *${task.title}*\n" +
                    "> Priority: `${task.priority.displayName}` | Assignee: `${task.assigneeName}` | Status: `${task.status.displayName}`"
            dispatchSlackWebhookInternal(config.slackWebhookUrl, slackMsg, config.slackChannel)
        }

        id
    }

    suspend fun updateTask(task: Task, actorName: String) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        activityDao.insertNotification(
            ActivityNotification(
                taskId = task.id,
                actorName = actorName,
                actionType = "UPDATED",
                description = "updated details for '${task.title}'",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    suspend fun updateTaskStatus(taskId: Long, newStatus: TaskStatus, actorName: String) = withContext(Dispatchers.IO) {
        val existing = taskDao.getTaskById(taskId) ?: return@withContext
        if (existing.status == newStatus) return@withContext

        val now = System.currentTimeMillis()
        taskDao.updateTaskStatus(taskId, newStatus, now)

        val desc = "moved '${existing.title}' to ${newStatus.displayName}"
        activityDao.insertNotification(
            ActivityNotification(
                taskId = taskId,
                actorName = actorName,
                actionType = if (newStatus == TaskStatus.DONE) "COMPLETED" else "STATUS_CHANGED",
                description = desc,
                timestamp = now,
                isRead = false
            )
        )

        if (newStatus == TaskStatus.DONE && existing.assigneeId != null) {
            teamMemberDao.incrementCompletedCount(existing.assigneeId)
        }

        // Automated Slack notification
        val config = integrationsDao.getConfig()
        if (config != null && config.isSlackEnabled && config.slackWebhookUrl.isNotBlank()) {
            val shouldNotify = (newStatus == TaskStatus.DONE && config.notifyOnDone) ||
                    (config.notifyOnStatusChange)
            if (shouldNotify) {
                val icon = if (newStatus == TaskStatus.DONE) ":white_check_mark:" else ":arrows_counterclockwise:"
                val slackMsg = "$icon *Task Status Update*: *${existing.title}* changed to `${newStatus.displayName}` by $actorName."
                dispatchSlackWebhookInternal(config.slackWebhookUrl, slackMsg, config.slackChannel)
            }
        }
    }

    suspend fun deleteTask(task: Task, actorName: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
        activityDao.insertNotification(
            ActivityNotification(
                taskId = null,
                actorName = actorName,
                actionType = "DELETED",
                description = "deleted task '${task.title}'",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    suspend fun addComment(taskId: Long, authorName: String, authorRole: UserRole, message: String) = withContext(Dispatchers.IO) {
        taskCommentDao.insertComment(
            TaskComment(
                taskId = taskId,
                authorName = authorName,
                authorRole = authorRole,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        )
        val task = taskDao.getTaskById(taskId)
        val taskTitle = task?.title ?: "Task #$taskId"
        activityDao.insertNotification(
            ActivityNotification(
                taskId = taskId,
                actorName = authorName,
                actionType = "COMMENTED",
                description = "commented on '$taskTitle': \"${message.take(45)}\"",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    suspend fun updateMemberRole(memberId: Long, newRole: UserRole, actorName: String) = withContext(Dispatchers.IO) {
        val member = teamMemberDao.getAllMembersFlow().firstOrNull()?.find { it.id == memberId } ?: return@withContext
        teamMemberDao.updateMember(member.copy(role = newRole))
        activityDao.insertNotification(
            ActivityNotification(
                taskId = null,
                actorName = actorName,
                actionType = "ROLE_CHANGED",
                description = "changed ${member.name}'s role to ${newRole.displayName}",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        activityDao.markAllAsRead()
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        activityDao.clearAll()
    }

    suspend fun saveIntegrationsConfig(config: IntegrationsConfig) = withContext(Dispatchers.IO) {
        integrationsDao.insertOrUpdate(config)
    }

    // Google Calendar Sync
    fun createGoogleCalendarIntent(task: Task): Intent {
        val startTime = task.dueDate ?: (System.currentTimeMillis() + 3600_000 * 24)
        val endTime = startTime + (task.estimatedHours.coerceAtLeast(1) * 3600_000L)

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "[TaskPulse] ${task.title}")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "${task.description}\n\nPriority: ${task.priority.displayName}\nAssignee: ${task.assigneeName}\nTags: ${task.tags}"
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            putExtra(CalendarContract.Events.EVENT_LOCATION, "TaskPulse Dashboard")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    suspend fun recordCalendarSync(count: Int, actorName: String) = withContext(Dispatchers.IO) {
        val currentConfig = integrationsDao.getConfig() ?: IntegrationsConfig()
        integrationsDao.insertOrUpdate(currentConfig.copy(lastCalendarSyncTime = System.currentTimeMillis()))
        activityDao.insertNotification(
            ActivityNotification(
                taskId = null,
                actorName = actorName,
                actionType = "SYNCED",
                description = "synced $count task(s) to Google Calendar",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    // Real-time simulated collaboration event
    suspend fun triggerSimulatedCollaborationEvent(): String = withContext(Dispatchers.IO) {
        val allTasks = taskDao.getAllTasksFlow().firstOrNull() ?: emptyList()
        val members = listOf("Sarah Miller", "Marcus Vance", "Elena Rostova", "Alex Chen")
        val randomMember = members.random()
        val actions = listOf(
            "moved task to In Review",
            "added feedback: 'Code review completed with 2 comments'",
            "logged 2 hours on sprint board",
            "updated priority to Urgent",
            "synced sprint milestones with Google Calendar"
        )
        val randomAction = actions.random()
        val randomTask = if (allTasks.isNotEmpty()) allTasks.random() else null

        val description = if (randomTask != null) {
            "$randomMember $randomAction for '${randomTask.title}'"
        } else {
            "$randomMember updated sprint board activity"
        }

        activityDao.insertNotification(
            ActivityNotification(
                taskId = randomTask?.id,
                actorName = randomMember,
                actionType = "LIVE_SYNC",
                description = description,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )

        description
    }

    // Slack Webhook dispatcher using OkHttp
    suspend fun sendSlackTest(config: IntegrationsConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (config.slackWebhookUrl.isBlank()) {
            return@withContext Pair(false, "Webhook URL cannot be empty")
        }
        val message = ":wave: *TaskPulse Webhook Test*! Automated team notifications are active for channel `${config.slackChannel}`."
        val result = dispatchSlackWebhookInternal(config.slackWebhookUrl, message, config.slackChannel)
        val statusMsg = if (result.first) "Success: Sent at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}" else "Error: ${result.second}"
        integrationsDao.insertOrUpdate(config.copy(lastSlackTestStatus = statusMsg))
        result
    }

    private fun dispatchSlackWebhookInternal(webhookUrl: String, text: String, channel: String): Pair<Boolean, String> {
        return try {
            val json = """{"text": ${escapeJson(text)}, "channel": ${escapeJson(channel)}}"""
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful || code == 200) {
                Pair(true, "Slack webhook accepted (HTTP $code)")
            } else {
                Pair(false, "HTTP $code: $responseBody")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to connect to Slack webhook")
        }
    }

    private fun escapeJson(str: String): String {
        return "\"" + str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }
}
