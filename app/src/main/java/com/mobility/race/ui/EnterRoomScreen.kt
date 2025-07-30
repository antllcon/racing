package com.mobility.race.ui

import SoundManager
import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.R
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation

@Composable
fun EnterRoomScreen(
    playerName: String,
    navigateToRoom: (String, String) -> Unit
) {
    LockScreenOrientation(Orientation.PORTRAIT)
    var roomName by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = "JOIN ROOM",
                    fontSize = 48.sp,
                    color = Color(0xFFFFA500),
                    fontFamily = FontFamily(Font(R.font.jersey25)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFF0000),
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )

                Text(
                    text = "Enter the room code to join your friends",
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
                modifier = Modifier.weight(1f)
            ) {
                AnimatedOutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = "ROOM CODE",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedButton(
                    onClick = {
                        if (playerName.isNotBlank() && roomName.isNotBlank()) {
                            navigateToRoom(playerName, roomName)
                        }
                    },
                    enabled = playerName.isNotBlank() && roomName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "JOIN NOW",
                        fontSize = 24.sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFFFFA500) else Color(0xAAFFFFFF),
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.jersey25)),
                fontSize = 16.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        },
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Text
        ),
        textStyle = TextStyle(
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.jersey25)),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(1f, 1f),
                blurRadius = 2f
            )
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0x66000000),
            unfocusedContainerColor = Color(0x66000000),
            focusedIndicatorColor = borderColor,
            unfocusedIndicatorColor = borderColor,
            cursorColor = Color(0xFFFFA500),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource
    )
}

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    soundManager: SoundManager? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val buttonState = isHovered || isFocused

    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f
            buttonState -> 1.05f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)
    )

    val color by animateColorAsState(
        targetValue = when {
            !enabled -> Color(0x66FF4500)
            buttonState -> Color(0xFFFF4500)
            else -> Color(0xCCFF0000)
        },
        animationSpec = tween(durationMillis = 200)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color(0x66FFA500)
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
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = Color(0x66FF4500),
            disabledContentColor = Color(0x99FFFFFF)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 12.dp,
            disabledElevation = 2.dp
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