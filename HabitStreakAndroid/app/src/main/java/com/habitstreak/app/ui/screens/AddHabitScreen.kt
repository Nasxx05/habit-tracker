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
import androidx.compose.material.icons.filled.Close
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
fun AddHabitScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("\u2B50") }
    val isDuplicate = viewModel.hasDuplicateName(habitName)
    val canSave = habitName.trim().isNotEmpty() && habitName.trim().length <= 50 && !isDuplicate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Habit", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.addHabit(habitName, selectedEmoji)
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
            // Habit Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Habit Name",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                OutlinedTextField(
                    value = habitName,
                    onValueChange = { if (it.length <= 50) habitName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Read for 30 minutes") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (isDuplicate) {
                    Text(
                        text = "A habit with this name already exists",
                        fontSize = 13.sp,
                        color = Color.Red
                    )
                }

                Text(
                    text = "${habitName.length}/50",
                    fontSize = 13.sp,
                    color = if (habitName.length > 50) Color.Red else GrayText,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Emoji Picker
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose Emoji",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                // Preview card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGreenBg)
                        .border(2.dp, SuccessGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedEmoji, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = habitName.ifEmpty { "Your Habit" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText,
                        maxLines = 1
                    )
                }

                // Emoji grid
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
        }
    }
}
