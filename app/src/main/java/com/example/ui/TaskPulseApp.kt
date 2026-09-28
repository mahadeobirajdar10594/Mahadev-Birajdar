package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UserRole
import com.example.ui.components.LiveActivityToast
import com.example.ui.components.RoleBadge
import com.example.ui.dialogs.CommandPaletteDialog
import com.example.ui.dialogs.NewTaskDialog
import com.example.ui.dialogs.ShortcutsDialog
import com.example.ui.dialogs.TaskDetailDialog
import com.example.ui.views.ActivityFeedView
import com.example.ui.views.AnalyticsView
import com.example.ui.views.BoardView
import com.example.ui.views.IntegrationsView
import com.example.ui.views.ListView
import com.example.ui.views.TeamRolesView

data class NavTabItem(
    val tab: DashboardTab,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPulseApp(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val liveToast by viewModel.liveToastMessage.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val showNewTask by viewModel.showNewTaskDialog.collectAsStateWithLifecycle()
    val showCommandPalette by viewModel.showCommandPalette.collectAsStateWithLifecycle()
    val showShortcuts by viewModel.showShortcutsDialog.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTaskForDetail.collectAsStateWithLifecycle()

    val unreadNotificationsCount = notifications.count { !it.isRead }

    val navItems = listOf(
        NavTabItem(DashboardTab.BOARD, Icons.Default.ViewKanban, "Board"),
        NavTabItem(DashboardTab.LIST, Icons.Default.FormatListBulleted, "List"),
        NavTabItem(DashboardTab.ANALYTICS, Icons.Default.Analytics, "Analytics"),
        NavTabItem(DashboardTab.ACTIVITY, Icons.Default.Notifications, "Activity"),
        NavTabItem(DashboardTab.INTEGRATIONS, Icons.Default.Hub, "Integrations"),
        NavTabItem(DashboardTab.TEAM, Icons.Default.People, "Team")
    )

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Hardware/Emulator Key listener for productivity shortcuts
    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.N -> viewModel.handleShortcutKey("N", context)
                        Key.B -> viewModel.handleShortcutKey("B", context)
                        Key.L -> viewModel.handleShortcutKey("L", context)
                        Key.A -> viewModel.handleShortcutKey("A", context)
                        Key.I -> viewModel.handleShortcutKey("I", context)
                        Key.T -> viewModel.handleShortcutKey("T", context)
                        Key.K -> viewModel.handleShortcutKey("K", context)
                        Key.R -> viewModel.handleShortcutKey("R", context)
                        else -> false
                    }
                } else false
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "TaskPulse",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                    },
                    actions = {
                        // Quick Search / Command Palette Bar
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.openCommandPalette() }
                                .padding(end = 6.dp)
                                .testTag("search_bar")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "⌘K",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Role Switcher Pill
                        RoleBadge(
                            role = activeRole,
                            onClick = { viewModel.setTab(DashboardTab.TEAM) },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("role_switcher")
                        )

                        // Dark/Light Theme Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Shortcuts Help Button
                        IconButton(
                            onClick = { viewModel.openShortcutsDialog() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("shortcuts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Shortcuts",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Notifications Bell with Badge
                        IconButton(
                            onClick = { viewModel.setTab(DashboardTab.ACTIVITY) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotificationsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val selected = currentTab == item.tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.setTab(item.tab) },
                            icon = {
                                if (item.tab == DashboardTab.ACTIVITY && unreadNotificationsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text("$unreadNotificationsCount") }
                                        }
                                    ) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = { Text(item.label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_${item.tab.name.lowercase()}")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (activeRole.canCreateTask && (currentTab == DashboardTab.BOARD || currentTab == DashboardTab.LIST)) {
                    FloatingActionButton(
                        onClick = { viewModel.openNewTaskDialog() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_task")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "New Task",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Live Activity Notification Banner (auto-dismissing real-time collaboration events)
                LiveActivityToast(
                    message = liveToast,
                    onDismiss = { /* dismissed */ }
                )

                // Tab Content
                when (currentTab) {
                    DashboardTab.BOARD -> BoardView(viewModel = viewModel)
                    DashboardTab.LIST -> ListView(viewModel = viewModel)
                    DashboardTab.ANALYTICS -> AnalyticsView(viewModel = viewModel)
                    DashboardTab.ACTIVITY -> ActivityFeedView(viewModel = viewModel)
                    DashboardTab.INTEGRATIONS -> IntegrationsView(viewModel = viewModel)
                    DashboardTab.TEAM -> TeamRolesView(viewModel = viewModel)
                }
            }
        }

        // Dialogs
        if (showNewTask) {
            NewTaskDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeNewTaskDialog() }
            )
        }

        val detailTask = selectedTask
        if (detailTask != null) {
            TaskDetailDialog(
                task = detailTask,
                viewModel = viewModel,
                onDismiss = { viewModel.selectTaskDetail(null) }
            )
        }

        if (showCommandPalette) {
            CommandPaletteDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeCommandPalette() }
            )
        }

        if (showShortcuts) {
            ShortcutsDialog(
                onDismiss = { viewModel.closeShortcutsDialog() }
            )
        }
    }

    // Handle Back Press when sub-screen or modal is up
    BackHandler(enabled = currentTab != DashboardTab.BOARD) {
        viewModel.setTab(DashboardTab.BOARD)
    }
}
