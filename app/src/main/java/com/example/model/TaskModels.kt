package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    DONE("Done")
}

enum class TaskPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class UserRole(val displayName: String, val badgeColorHex: String) {
    ADMIN("Admin", "#EF4444"),
    MANAGER("Manager", "#8B5CF6"),
    MEMBER("Member", "#06B6D4"),
    VIEWER("Viewer", "#64748B");

    val canCreateTask: Boolean get() = this != VIEWER
    val canEditTask: Boolean get() = this != VIEWER
    val canDeleteTask: Boolean get() = this == ADMIN || this == MANAGER
    val canManageIntegrations: Boolean get() = this == ADMIN
    val canAssignRoles: Boolean get() = this == ADMIN
}

enum class PresenceStatus(val label: String, val colorHex: String) {
    ONLINE("Online", "#10B981"),
    FOCUS("Focus Mode", "#8B5CF6"),
    AWAY("Away", "#F59E0B"),
    OFFLINE("Offline", "#94A3B8")
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val assigneeId: Long? = null,
    val assigneeName: String = "Unassigned",
    val dueDate: Long? = null, // epoch millis
    val tags: String = "Feature", // comma separated
    val estimatedHours: Int = 4,
    val spentHours: Int = 0,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.MEMBER,
    val avatarColorHex: String = "#6366F1",
    val presence: PresenceStatus = PresenceStatus.ONLINE,
    val completedTasks: Int = 0
)

@Entity(tableName = "task_comments")
data class TaskComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val authorName: String,
    val authorRole: UserRole,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_notifications")
data class ActivityNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val actorName: String,
    val actionType: String, // CREATED, MOVED, COMPLETED, COMMENTED, SYNCED
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "integrations_config")
data class IntegrationsConfig(
    @PrimaryKey
    val id: Int = 1,
    val slackWebhookUrl: String = "",
    val slackChannel: String = "#proj-taskpulse",
    val isSlackEnabled: Boolean = false,
    val notifyOnCreate: Boolean = true,
    val notifyOnStatusChange: Boolean = true,
    val notifyOnDone: Boolean = true,
    val isGoogleCalendarEnabled: Boolean = true,
    val autoSyncDueDates: Boolean = true,
    val lastSlackTestStatus: String = "Not tested",
    val lastCalendarSyncTime: Long? = null
)
