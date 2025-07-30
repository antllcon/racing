package com.mobility.race.ui

import SoundManager
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.R
import androidx.compose.ui.text.TextStyle
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation

@Composable
fun RaceFinishedScreen(
    finishTime: Long,
    lapsCompleted: Int,
    totalLaps: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    soundManager: SoundManager
) {
    LockScreenOrientation(Orientation.PORTRAIT)
    val onExitWithMusic: () -> Unit = {
        soundManager.playMenuMusic()
        onExit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.finished_room),
                contentScale = ContentScale.Crop
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0xE6000000),
                            Color(0xCC000000)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "RACE FINISHED!",
                color = Color(0xFFFFA500),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.jersey25)),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xAAFF0000),
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0x66000000),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "YOUR RESULTS",
                    color = Color(0xFFFFA500),
                    fontSize = 22.sp,
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    fontWeight = FontWeight.Bold
                )

                ResultItem(
                    label = "LAPS COMPLETED",
                    value = "$lapsCompleted/$totalLaps"
                )

                ResultItem(
                    label = "FINISH TIME",
                    value = formatTime(finishTime)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    soundManager = soundManager
                ) {
                    Text(
                        text = "RESTART RACE",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedButton(
                    onClick = onExitWithMusic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    soundManager = soundManager
                ) {
                    Text(
                        text = "RETURN TO MENU",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFFFFA500),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.jersey25)),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.jersey25)),
            fontWeight = FontWeight.Bold
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d.%03d", minutes, remainingSeconds, millis % 1000)
}