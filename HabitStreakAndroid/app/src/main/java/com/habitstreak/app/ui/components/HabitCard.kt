package com.habitstreak.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.model.Habit
import com.habitstreak.app.ui.theme.*

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = habit.isCompletedToday
    var showMenu by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) LightGreenBg else LightGray,
        animationSpec = tween(300),
        label = "card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) SuccessGreen else BorderGray,
        animationSpec = tween(300),
        label = "card_border"
    )

    var checkBounce by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (checkBounce) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "check_scale",
        finishedListener = { checkBounce = false }
    )

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.08f))
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable {
                    checkBounce = true
                    onToggle()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(text = habit.emoji, fontSize = 32.sp)

            Spacer(modifier = Modifier.width(14.dp))

            // Name and streak
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (habit.currentStreak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = habit.streakFireEmojis, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.currentStreak} day${if (habit.currentStreak == 1) "" else "s"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                habit.currentStreak >= 30 -> Gold
                                habit.currentStreak >= 7 -> StreakOrange
                                else -> GrayText
                            }
                        )
                    }
                } else {
                    Text(
                        text = "Start your streak!",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            }

            // Checkmark
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(if (isCompleted) SuccessGreen else Color.White)
                    .border(2.dp, if (isCompleted) SuccessGreen else BorderGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Context menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Habit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Habit", color = Color.Red) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                )
            }
        }

        // Long press trigger area (invisible, over the card)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        checkBounce = true
                        onToggle()
                    },
                    onClickLabel = "Toggle habit"
                )
        ) {
            // The context menu trigger via long press
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .clickable { showMenu = true }
            )
        }
    }
}
