package com.mobility.race.ui

import SoundManager
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

@Composable
fun MultiplayerRaceFinishedScreen(
    playerResults: List<PlayerResult>,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    soundManager: SoundManager
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(500.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color(0xCC000000),
                            Color(0x99000000)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RACE FINISHED!",
                    color = Color(0xFFFFA500),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFF0000),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FINAL RESULTS",
                    color = Color(0xFFFFA500),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = Color(0x66000000),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(playerResults) { index, result ->
                        PlayerResultItem(
                            position = index + 1,
                            playerName = result.playerName,
                            finishTime = result.finishTime,
                            isCurrentPlayer = result.isCurrentPlayer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(60.dp),
                    soundManager = soundManager
                ) {
                    Text(
                        text = "REMATCH",
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedButton(
                    onClick = onExitWithMusic,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(60.dp),
                    soundManager = soundManager
                ) {
                    Text(
                        text = "RETURN TO MENU",
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerResultItem(
    position: Int,
    playerName: String,
    finishTime: Long,
    isCurrentPlayer: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrentPlayer) Color(0x44FFA500) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$position. $playerName",
            color = if (isCurrentPlayer) Color(0xFFFFA500) else Color.White,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.jersey25))
        )

        Text(
            text = formatTime(finishTime),
            color = if (isCurrentPlayer) Color(0xFFFFA500) else Color.White,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.jersey25))
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

data class PlayerResult(
    val playerName: String,
    val finishTime: Long,
    val isCurrentPlayer: Boolean
)