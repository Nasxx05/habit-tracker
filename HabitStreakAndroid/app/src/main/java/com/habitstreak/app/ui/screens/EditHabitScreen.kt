package com.habitstreak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.ui.theme.*
import com.habitstreak.app.viewmodel.HabitViewModel

private val emojiOptions = listOf(
    "\uD83D\uDCAA", "\uD83D\uDCDA", "\uD83C\uDFC3", "\uD83D\uDCA7", "\uD83E\uDDD8", "\u270D\uFE0F", "\uD83C\uDFAF",
    "\uD83C\uDFA8", "\uD83C\uDFB5", "\uD83C\uDF31", "\uD83D\uDD25", "\u2B50", "\uD83D\uDCA1", "\u2764\uFE0F",
    "\uD83D\uDEB4", "\uD83D\uDCDD", "\uD83C\uDF4E", "\uD83D\uDEB6", "\uD83D\uDCBB", "\uD83D\uDE0A"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    viewModel: HabitViewModel,
    habitId: String,
    onBack: () -> Unit
) {
    val habit = viewModel.habits.find { it.id == habitId }
    if (habit == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var name by remember { mutableStateOf(habit.name) }
    var selectedEmoji by remember { mutableStateOf(habit.emoji) }
    val isDuplicate = viewModel.hasDuplicateName(name, excludeId = habitId)
    val canSave = name.trim().isNotEmpty() && !isDuplicate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateHabit(habitId, name, selectedEmoji)
                            onBack()
                        },
                        enabled = canSave
                    ) {
                        Text(
                            "Save",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (canSave) PrimaryBlue else GrayText
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Habit Name", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 50) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                if (isDuplicate) {
                    Text("A habit with this name already exists", fontSize = 13.sp, color = Color.Red)
                }
            }

            // Emoji picker
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose Emoji", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGray)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojiOptions) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedEmoji == emoji) PrimaryBlue.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .then(
                                    if (selectedEmoji == emoji)
                                        Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                    }
                }
            }

            // Stats
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stats", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LightGray
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatRow("Current Streak", "${habit.currentStreak} days")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow("Longest Streak", "${habit.longestStreak} days")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow("Total Completions", "${habit.completionDates.size}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = DarkText)
        Text(text = value, fontSize = 16.sp, color = GrayText)
    }
}
