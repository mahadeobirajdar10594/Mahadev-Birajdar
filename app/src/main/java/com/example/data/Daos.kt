package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ActivityNotification
import com.example.model.IntegrationsConfig
import com.example.model.Task
import com.example.model.TaskComment
import com.example.model.TaskStatus
import com.example.model.TeamMember
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY orderIndex ASC, id DESC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAt: Long)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY id ASC")
    fun getAllMembersFlow(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TeamMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<TeamMember>)

    @Update
    suspend fun updateMember(member: TeamMember)

    @Query("UPDATE team_members SET completedTasks = completedTasks + 1 WHERE id = :memberId")
    suspend fun incrementCompletedCount(memberId: Long)
}

@Dao
interface TaskCommentDao {
    @Query("SELECT * FROM task_comments WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun getCommentsForTaskFlow(taskId: Long): Flow<List<TaskComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: TaskComment): Long
}

@Dao
interface ActivityNotificationDao {
    @Query("SELECT * FROM activity_notifications ORDER BY timestamp DESC LIMIT 60")
    fun getAllNotificationsFlow(): Flow<List<ActivityNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ActivityNotification): Long

    @Query("UPDATE activity_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM activity_notifications")
    suspend fun clearAll()
}

@Dao
interface IntegrationsDao {
    @Query("SELECT * FROM integrations_config WHERE id = 1")
    fun getConfigFlow(): Flow<IntegrationsConfig?>

    @Query("SELECT * FROM integrations_config WHERE id = 1")
    suspend fun getConfig(): IntegrationsConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: IntegrationsConfig)
}
