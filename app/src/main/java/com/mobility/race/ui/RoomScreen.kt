package com.mobility.race.ui

import SoundManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.presentation.multiplayer.RoomViewModel
import com.mobility.race.R
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation

@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    soundManager: SoundManager? = null
) {
    val state = viewModel.state.value
    LockScreenOrientation(Orientation.PORTRAIT)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.enter_room_background_image),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )
            .background(Color(0x99000000))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "Room Code: ${state.roomId}",
                fontSize = 24.sp,
                color = Color(0xFFFFA500),
                fontFamily = FontFamily(Font(R.font.jersey25)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color(0xAA000000),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x66000000))
                    .border(
                        width = 2.dp,
                        color = Color(0xAAFFA500),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Players",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.playerNames) { name ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33FFFFFF))
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .clickable {
                                        soundManager?.playClickSound()
                                    }
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(R.font.jersey25)),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isCreatingRoom && state.playerNames.size > 1) {
                AnimatedButton(
                    onClick = {
                        soundManager?.playClickSound()
                        viewModel.startGame()
                    },
                    soundManager = soundManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "START GAME",
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}