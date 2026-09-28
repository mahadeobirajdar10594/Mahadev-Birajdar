package com.example

import com.example.model.PresenceStatus
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.TaskStatus
import com.example.model.TeamMember
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskPulseUnitTest {

    @Test
    fun testRolePermissions() {
        // Admin
        assertTrue(UserRole.ADMIN.canCreateTask)
        assertTrue(UserRole.ADMIN.canEditTask)
        assertTrue(UserRole.ADMIN.canDeleteTask)
        assertTrue(UserRole.ADMIN.canManageIntegrations)
        assertTrue(UserRole.ADMIN.canAssignRoles)

        // Manager
        assertTrue(UserRole.MANAGER.canCreateTask)
        assertTrue(UserRole.MANAGER.canEditTask)
        assertTrue(UserRole.MANAGER.canDeleteTask)
        assertFalse(UserRole.MANAGER.canManageIntegrations)
        assertFalse(UserRole.MANAGER.canAssignRoles)

        // Member
        assertTrue(UserRole.MEMBER.canCreateTask)
        assertTrue(UserRole.MEMBER.canEditTask)
        assertFalse(UserRole.MEMBER.canDeleteTask)
        assertFalse(UserRole.MEMBER.canManageIntegrations)
        assertFalse(UserRole.MEMBER.canAssignRoles)

        // Viewer (Read-only)
        assertFalse(UserRole.VIEWER.canCreateTask)
        assertFalse(UserRole.VIEWER.canEditTask)
        assertFalse(UserRole.VIEWER.canDeleteTask)
        assertFalse(UserRole.VIEWER.canManageIntegrations)
        assertFalse(UserRole.VIEWER.canAssignRoles)
    }

    @Test
    fun testTaskDataModel() {
        val task = Task(
            id = 42,
            title = "Test Slack Integration",
            description = "Test automated webhook dispatching",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
            assigneeName = "Sarah Miller",
            tags = "Integration,Slack",
            estimatedHours = 6,
            spentHours = 2
        )

        assertEquals(42L, task.id)
        assertEquals("Test Slack Integration", task.title)
        assertEquals(TaskStatus.IN_PROGRESS, task.status)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals("Sarah Miller", task.assigneeName)
        assertEquals(6, task.estimatedHours)
        assertEquals(2, task.spentHours)
    }

    @Test
    fun testTeamMemberPresence() {
        val member = TeamMember(
            id = 1,
            name = "Alex Chen",
            email = "alex.chen@taskpulse.io",
            role = UserRole.ADMIN,
            presence = PresenceStatus.ONLINE,
            completedTasks = 10
        )

        assertEquals("Online", member.presence.label)
        assertEquals(UserRole.ADMIN, member.role)
        assertEquals(10, member.completedTasks)
    }
}
