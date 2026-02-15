package com.habitstreak.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.ui.theme.*
import com.habitstreak.app.util.DateHelper
import com.habitstreak.app.viewmodel.DayCompletionStatus
import com.habitstreak.app.viewmodel.HabitViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val currentMonth = YearMonth.now()
    val canGoForward = displayedMonth.isBefore(currentMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    displayedMonth = displayedMonth.minusMonths(1)
                    selectedDate = null
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = PrimaryBlue)
                }

                Text(
                    text = DateHelper.monthYearString(displayedMonth),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                IconButton(
                    onClick = {
                        displayedMonth = displayedMonth.plusMonths(1)
                        selectedDate = null
                    },
                    enabled = canGoForward
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = if (canGoForward) PrimaryBlue else GrayText
                    )
                }
            }

            // Calendar grid
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LightGray
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Weekday headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val days = listOf(
                            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                            DayOfWeek.SATURDAY
                        )
                        days.forEach { day ->
                            Text(
                                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GrayText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day cells
                    val firstDay = displayedMonth.atDay(1)
                    val offset = firstDay.dayOfWeek.value % 7 // Sunday = 0
                    val daysInMonth = displayedMonth.lengthOfMonth()
                    val totalCells = offset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - offset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val date = displayedMonth.atDay(dayNumber)
                                    val status = viewModel.dayStatus(date)
                                    val isToday = date == LocalDate.now()
                                    val isSelected = selectedDate == date

                                    DayCell(
                                        day = dayNumber,
                                        status = status,
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (status != DayCompletionStatus.FUTURE) {
                                                selectedDate = if (isSelected) null else date
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Selected day detail
            AnimatedVisibility(
                visible = selectedDate != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDate?.let { date ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LightGray
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = DateHelper.formattedDate(date),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            viewModel.habits.forEach { habit ->
                                if (!date.isBefore(habit.createdDate)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = habit.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = habit.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DarkText,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (habit.completedOn(date)) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Completed",
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Outlined.Circle,
                                                contentDescription = "Not completed",
                                                tint = GrayText,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stats
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LightGray
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Stats This Month",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatItem(
                        icon = "\u2705",
                        label = "Perfect days",
                        value = "${viewModel.perfectDays(displayedMonth)}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        icon = "\uD83D\uDCCA",
                        label = "Completion rate",
                        value = "${(viewModel.completionRate(displayedMonth) * 100).toInt()}%"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        icon = "\uD83D\uDD25",
                        label = "Best streak",
                        value = "${viewModel.bestStreak()} days"
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    status: DayCompletionStatus,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> PrimaryBlue.copy(alpha = 0.1f)
        isToday -> PrimaryBlue.copy(alpha = 0.08f)
        status == DayCompletionStatus.ALL_COMPLETED -> SuccessGreen.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    val textColor = when {
        status == DayCompletionStatus.FUTURE -> BorderGray
        isToday -> PrimaryBlue
        else -> DarkText
    }

    val dotColor = when (status) {
        DayCompletionStatus.ALL_COMPLETED -> SuccessGreen
        DayCompletionStatus.SOME_COMPLETED -> SuccessGreen.copy(alpha = 0.4f)
        DayCompletionStatus.NONE_COMPLETED -> BorderGray
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(enabled = status != DayCompletionStatus.FUTURE) { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$day",
            fontSize = 16.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@Composable
private fun StatItem(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GrayText
        )
    }
}
