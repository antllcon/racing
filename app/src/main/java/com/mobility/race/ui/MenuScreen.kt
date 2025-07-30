package com.mobility.race.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
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
                var isIconHovered by remember { mutableStateOf(false) }
                val iconScale by animateFloatAsState(
                    targetValue = if (isIconHovered) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                )

                Image(
                    painter = painterResource(R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(150.dp)
                        .scale(iconScale)
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
                AnimatedButton(
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

                AnimatedButton(
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
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    soundManager: SoundManager? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val buttonState = isHovered || isFocused

    val scale by animateFloatAsState(
        targetValue = if (buttonState) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)
    )

    val color by animateColorAsState(
        targetValue = when {
            buttonState -> Color(0xFFFF4500)
            else -> Color(0xCCFF0000)
        },
        animationSpec = tween(durationMillis = 200)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            buttonState -> Color(0xAAFFFF00)
            else -> Color(0xAAFFA500)
        },
        animationSpec = tween(durationMillis = 200)
    )

    Button(
        onClick = {
            soundManager?.playClickSound()
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(70.dp)
            .clip(MaterialTheme.shapes.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 12.dp
        ),
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}