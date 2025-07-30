package com.mobility.race.ui

import SoundManager
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation
import com.mobility.race.R
import androidx.compose.ui.text.TextStyle

@Composable
fun MultiplayerMenuScreen(
    navigateToJoinRoom: (String) -> Unit,
    navigateToCreateRoom: (String, String) -> Unit,
    soundManager: SoundManager? = null,
) {
    LockScreenOrientation(Orientation.PORTRAIT)
    var playerName by remember { mutableStateOf("") }
    var newRoomName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.enter_room_background_image),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "MULTIPLAYER",
                    fontSize = 48.sp,
                    color = Color(0xFFFFA500),
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFF0000),
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )

                Text(
                    text = "Race against friends in real-time",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.5f)
            ) {
                AnimatedOutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = "YOUR NAME",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedButton(
                    onClick = {
                        if (playerName.isNotEmpty()) {
                            navigateToJoinRoom(playerName)
                        }
                    },
                    soundManager = soundManager,
                    enabled = playerName.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "JOIN EXISTING RACE",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(
                        color = Color(0xAAFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "OR",
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )

                    Divider(
                        color = Color(0xAAFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedOutlinedTextField(
                    value = newRoomName,
                    onValueChange = { newRoomName = it },
                    label = "NEW ROOM NAME",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedButton(
                    onClick = {
                        if (newRoomName.isNotEmpty() && playerName.isNotEmpty()) {
                            navigateToCreateRoom(playerName, newRoomName)
                        }
                    },
                    enabled = newRoomName.isNotEmpty() && playerName.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "CREATE NEW RACE",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
fun Divider(
    color: Color,
    thickness: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}