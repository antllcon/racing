package com.mobility.race.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.R
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation
import SoundManager

@Composable
fun MenuScreen(
    navigateToSingleplayer: () -> Unit,
    navigateToMultiplayerMenuScreen: () -> Unit,
    soundManager: SoundManager
) {
    LockScreenOrientation(Orientation.PORTRAIT)

    LaunchedEffect(Unit) {
        soundManager.playMenuMusic()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.background_image),
                contentScale = ContentScale.Crop,
                alpha = 0.8f
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUST RACE!",
                    fontSize = 64.sp,
                    color = Color(0xFFFF4500),
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFF0000),
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SimpleButton(
                    onClick = navigateToSingleplayer,
                    soundManager = soundManager,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "SINGLEPLAYER",
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xAAFFA500),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                SimpleButton(
                    onClick = navigateToMultiplayerMenuScreen,
                    soundManager = soundManager,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "MULTIPLAYER",
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.jersey25)),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xAAFFA500),
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
fun SimpleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    soundManager: SoundManager? = null,
    content: @Composable () -> Unit
) {
    Button(
        onClick = {
            soundManager?.playClickSound()
            onClick()
        },
        modifier = modifier.height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xCCFF0000),
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xAAFFA500)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}