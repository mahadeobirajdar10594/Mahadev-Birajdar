package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresenceStatus
import com.example.model.TaskPriority
import com.example.model.TaskStatus
import com.example.model.UserRole
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityUrgent
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusInReview
import com.example.ui.theme.StatusTodo

@Composable
fun UserAvatar(
    name: String,
    avatarColorHex: String = "#6366F1",
    presence: PresenceStatus? = null,
    sizeDp: Int = 36,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(avatarColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(modifier = modifier.size(sizeDp.dp)) {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.4).sp
            )
        }

        if (presence != null) {
            val dotColor = when (presence) {
                PresenceStatus.ONLINE -> Color(0xFF10B981)
                PresenceStatus.FOCUS -> Color(0xFF8B5CF6)
                PresenceStatus.AWAY -> Color(0xFFF59E0B)
                PresenceStatus.OFFLINE -> Color(0xFF94A3B8)
            }
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.32).dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority) {
        TaskPriority.LOW -> Pair(PriorityLow.copy(alpha = 0.18f), PriorityLow)
        TaskPriority.MEDIUM -> Pair(PriorityMedium.copy(alpha = 0.18f), PriorityMedium)
        TaskPriority.HIGH -> Pair(PriorityHigh.copy(alpha = 0.18f), PriorityHigh)
        TaskPriority.URGENT -> Pair(PriorityUrgent.copy(alpha = 0.22f), PriorityUrgent)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = priority.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        TaskStatus.TODO -> Pair(StatusTodo.copy(alpha = 0.2f), StatusTodo)
        TaskStatus.IN_PROGRESS -> Pair(StatusInProgress.copy(alpha = 0.2f), StatusInProgress)
        TaskStatus.IN_REVIEW -> Pair(StatusInReview.copy(alpha = 0.2f), StatusInReview)
        TaskStatus.DONE -> Pair(StatusDone.copy(alpha = 0.2f), StatusDone)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val badgeColor = try {
        Color(android.graphics.Color.parseColor(role.badgeColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = role.displayName,
                color = badgeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LiveActivityToast(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("live_activity_toast")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Live Activity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
