package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ActivityNotification
import com.example.model.IntegrationsConfig
import com.example.model.PresenceStatus
import com.example.model.Task
import com.example.model.TaskComment
import com.example.model.TaskPriority
import com.example.model.TaskStatus
import com.example.model.TeamMember
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Task::class,
        TeamMember::class,
        TaskComment::class,
        ActivityNotification::class,
        IntegrationsConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun taskCommentDao(): TaskCommentDao
    abstract fun activityNotificationDao(): ActivityNotificationDao
    abstract fun integrationsDao(): IntegrationsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskpulse_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val day = 86_400_000L

            // Initial Team Members
            val members = listOf(
                TeamMember(id = 1, name = "Alex Chen", email = "alex.chen@taskpulse.io", role = UserRole.ADMIN, avatarColorHex = "#6366F1", presence = PresenceStatus.ONLINE, completedTasks = 18),
                TeamMember(id = 2, name = "Sarah Miller", email = "sarah.m@taskpulse.io", role = UserRole.MANAGER, avatarColorHex = "#EC4899", presence = PresenceStatus.ONLINE, completedTasks = 24),
                TeamMember(id = 3, name = "Marcus Vance", email = "marcus.v@taskpulse.io", role = UserRole.MEMBER, avatarColorHex = "#06B6D4", presence = PresenceStatus.FOCUS, completedTasks = 15),
                TeamMember(id = 4, name = "Elena Rostova", email = "elena.r@taskpulse.io", role = UserRole.MEMBER, avatarColorHex = "#10B981", presence = PresenceStatus.AWAY, completedTasks = 12),
                TeamMember(id = 5, name = "David Kim", email = "david.k@taskpulse.io", role = UserRole.VIEWER, avatarColorHex = "#F59E0B", presence = PresenceStatus.OFFLINE, completedTasks = 4)
            )
            database.teamMemberDao().insertAll(members)

            // Initial Tasks
            val tasks = listOf(
                Task(
                    id = 1,
                    title = "Real-time Webhook Event Pipeline",
                    description = "Set up real-time websocket and Slack incoming webhook triggers for automated channel updates on sprint events.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.URGENT,
                    assigneeId = 1,
                    assigneeName = "Alex Chen",
                    dueDate = now + (day * 2),
                    tags = "Backend,Slack,API",
                    estimatedHours = 8,
                    spentHours = 5,
                    orderIndex = 0
                ),
                Task(
                    id = 2,
                    title = "Interactive Kanban Drag-and-Drop",
                    description = "Refine gesture detection, visual lift shadows, and multi-column reordering for responsive board interactions.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.HIGH,
                    assigneeId = 3,
                    assigneeName = "Marcus Vance",
                    dueDate = now + (day * 3),
                    tags = "Compose,UX,Kanban",
                    estimatedHours = 12,
                    spentHours = 7,
                    orderIndex = 1
                ),
                Task(
                    id = 3,
                    title = "Google Calendar Sync Engine",
                    description = "Enable 2-way event synchronization using Android CalendarContract for automatic deadline reminders.",
                    status = TaskStatus.IN_REVIEW,
                    priority = TaskPriority.HIGH,
                    assigneeId = 2,
                    assigneeName = "Sarah Miller",
                    dueDate = now + (day * 1),
                    tags = "Google,Sync,Calendar",
                    estimatedHours = 6,
                    spentHours = 6,
                    orderIndex = 0
                ),
                Task(
                    id = 4,
                    title = "Analytics & Velocity Tracking",
                    description = "Generate sprint burndown charts, team throughput metrics, and resolution cycle time reports.",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.MEDIUM,
                    assigneeId = 4,
                    assigneeName = "Elena Rostova",
                    dueDate = now + (day * 5),
                    tags = "Analytics,Reports,Metrics",
                    estimatedHours = 10,
                    spentHours = 0,
                    orderIndex = 0
                ),
                Task(
                    id = 5,
                    title = "Role-Based Access Control Audit",
                    description = "Verify permissions matrix across Admin, Manager, Member, and Viewer accounts with real-time UI feedback.",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    assigneeId = 1,
                    assigneeName = "Alex Chen",
                    dueDate = now + (day * 4),
                    tags = "Security,Roles,Audit",
                    estimatedHours = 4,
                    spentHours = 1,
                    orderIndex = 1
                ),
                Task(
                    id = 6,
                    title = "Dark Theme & Command Palette",
                    description = "Implement sleek M3 dark palette, high contrast tokens, and Ctrl+K fast action shortcuts.",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.MEDIUM,
                    assigneeId = 3,
                    assigneeName = "Marcus Vance",
                    dueDate = now - day,
                    tags = "UI,Shortcuts,Theme",
                    estimatedHours = 6,
                    spentHours = 6,
                    orderIndex = 0
                ),
                Task(
                    id = 7,
                    title = "Live Activity Notification Stream",
                    description = "In-app toast alerts, badge counts, and unread filter tabs for active team events.",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.HIGH,
                    assigneeId = 2,
                    assigneeName = "Sarah Miller",
                    dueDate = now - (day * 2),
                    tags = "Notifications,Live,Collaboration",
                    estimatedHours = 8,
                    spentHours = 8,
                    orderIndex = 1
                )
            )
            database.taskDao().insertAll(tasks)

            // Initial Comments
            database.taskCommentDao().insertComment(
                TaskComment(
                    taskId = 1,
                    authorName = "Sarah Miller",
                    authorRole = UserRole.MANAGER,
                    message = "Webhook payload schema verified. Ready for staging test on #proj-taskpulse.",
                    timestamp = now - (3600_000 * 3)
                )
            )
            database.taskCommentDao().insertComment(
                TaskComment(
                    taskId = 3,
                    authorName = "Alex Chen",
                    authorRole = UserRole.ADMIN,
                    message = "Google Calendar sync handles recurring dates properly now. Moving to Review!",
                    timestamp = now - (3600_000 * 1)
                )
            )

            // Initial Notifications
            val notifications = listOf(
                ActivityNotification(
                    taskId = 3,
                    actorName = "Sarah Miller",
                    actionType = "STATUS_CHANGED",
                    description = "moved 'Google Calendar Sync Engine' to In Review",
                    timestamp = now - (1000 * 60 * 15),
                    isRead = false
                ),
                ActivityNotification(
                    taskId = 1,
                    actorName = "Alex Chen",
                    actionType = "SLACK_ALERT_SENT",
                    description = "dispatched automated Slack alert for 'Real-time Webhook Event Pipeline'",
                    timestamp = now - (1000 * 60 * 42),
                    isRead = false
                ),
                ActivityNotification(
                    taskId = 6,
                    actorName = "Marcus Vance",
                    actionType = "COMPLETED",
                    description = "completed 'Dark Theme & Command Palette' (ahead of sprint schedule!)",
                    timestamp = now - (1000 * 60 * 95),
                    isRead = true
                ),
                ActivityNotification(
                    taskId = 2,
                    actorName = "Marcus Vance",
                    actionType = "COMMENTED",
                    description = "commented on 'Interactive Kanban Drag-and-Drop'",
                    timestamp = now - (1000 * 60 * 180),
                    isRead = true
                )
            )
            notifications.forEach { database.activityNotificationDao().insertNotification(it) }

            // Default Integrations Config
            database.integrationsDao().insertOrUpdate(
                IntegrationsConfig(
                    id = 1,
                    slackWebhookUrl = "https://hooks.slack.com/services/DEMO/T000/B000TASKPULSE",
                    slackChannel = "#proj-taskpulse",
                    isSlackEnabled = true,
                    notifyOnCreate = true,
                    notifyOnStatusChange = true,
                    notifyOnDone = true,
                    isGoogleCalendarEnabled = true,
                    autoSyncDueDates = true,
                    lastSlackTestStatus = "Ready to test",
                    lastCalendarSyncTime = now - (3600_000 * 2)
                )
            )
        }
    }
}
