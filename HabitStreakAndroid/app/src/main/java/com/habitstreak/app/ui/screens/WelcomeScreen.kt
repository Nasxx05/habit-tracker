package com.habitstreak.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.ui.theme.*
import com.habitstreak.app.util.DateHelper
import com.habitstreak.app.viewmodel.HabitViewModel
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    viewModel: HabitViewModel,
    isFirstLaunch: Boolean,
    onContinue: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        if (!isFirstLaunch) {
            delay(2000)
            onContinue()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "welcome_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(WelcomeGradientStart, WelcomeGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            if (isFirstLaunch) {
                FirstLaunchContent()
            } else {
                ReturningUserContent(viewModel)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isFirstLaunch) viewModel.markFirstLaunchDone()
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = if (isFirstLaunch) "Get Started" else "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun FirstLaunchContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "\uD83D\uDD25", fontSize = 80.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Streaks!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Build better habits,\none day at a time",
            fontSize = 18.sp,
            color = GrayText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReturningUserContent(viewModel: HabitViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = DateHelper.timeOfDay().greeting,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.habits.isEmpty()) {
            Text(
                text = "Ready to start tracking?",
                fontSize = 20.sp,
                color = GrayText
            )
        } else if (viewModel.allCompletedToday) {
            Text(
                text = "All done for today! \uD83C\uDF89",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SuccessGreen
            )
        } else {
            val completed = viewModel.completedTodayCount
            val remaining = viewModel.remainingTodayCount
            if (completed > 0) {
                Text(
                    text = "$completed habit${if (completed == 1) "" else "s"} completed\n$remaining more to go!",
                    fontSize = 20.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "You're on a roll!\nLet's keep it going",
                    fontSize = 20.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
