package com.habitstreak.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.model.Habit
import com.habitstreak.app.ui.components.ConfettiAnimation
import com.habitstreak.app.ui.components.HabitCard
import com.habitstreak.app.ui.theme.*
import com.habitstreak.app.util.DateHelper
import com.habitstreak.app.viewmodel.HabitViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onNavigateToAddHabit: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToEditHabit: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.showMilestoneConfetti.value) {
        if (viewModel.showMilestoneConfetti.value) {
            showConfetti = true
            delay(2500)
            showConfetti = false
            viewModel.showMilestoneConfetti.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habit Streaks",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "\uD83D\uDCC5 ", fontSize = 16.sp)
                    Text(
                        text = DateHelper.formattedDate(),
                        fontSize = 16.sp,
                        color = GrayText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = DateHelper.dailyMotivationalMessage(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayText
                )
            }

            // Habit list
            if (viewModel.habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "\uD83C\uDF31", fontSize = 60.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No habits yet",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first habit to start\nbuilding streaks!",
                            fontSize = 16.sp,
                            color = GrayText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onToggle = { viewModel.toggleHabit(habit.id) },
                            onEdit = { onNavigateToEditHabit(habit.id) },
                            onDelete = { showDeleteDialog = habit.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Add button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, ambientColor = Color.Black.copy(alpha = 0.05f))
                    .background(Background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToAddHabit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Habit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Confetti overlay
        if (showConfetti) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize()
            )
        }

        // Milestone toast
        AnimatedVisibility(
            visible = viewModel.showMilestoneConfetti.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(listOf(StreakOrange, Gold)),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = viewModel.milestoneMessage.value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Delete confirmation dialog
        showDeleteDialog?.let { habitId ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Habit?") },
                text = { Text("This will permanently delete this habit and all its history.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteHabit(habitId)
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
