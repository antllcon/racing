package com.mobility.race.ui

import SoundManager
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.ui.drawUtils.LifecycleEventHandler
import com.mobility.race.ui.drawUtils.bitmapStorage
import com.mobility.race.ui.drawUtils.drawBackgroundTexture
import com.mobility.race.ui.drawUtils.drawCars
import com.mobility.race.ui.drawUtils.drawControllingStick
import com.mobility.race.ui.drawUtils.drawGameMap
import com.mobility.race.ui.drawUtils.drawMinimap
import com.mobility.race.ui.drawUtils.drawNextCheckpoint
import com.mobility.race.util.findActivity

@Composable
fun MultiplayerGameScreen(
    viewModel: MultiplayerGameViewModel,
    soundManager: SoundManager,
    onBack: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        soundManager.pauseBackgroundMusic()
    }

    LifecycleEventHandler(
        onPause = { viewModel.soundManager.pauseBackgroundMusic() },
        onResume = { viewModel.soundManager.resumeBackgroundMusic() }
    )

    val context = LocalContext.current
    val state = viewModel.state.value
    val bitmaps = bitmapStorage(context)

    var isStickActive by remember { mutableStateOf(false) }
    var currentStickInputAngle: Float? by remember { mutableStateOf(null) }
    var currentStickInputDistanceFactor: Float by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.countdown > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = (state.countdown).toString())
            }
            return@Box
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    state.controllingStick.setScreenSize(
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                    state.gameCamera.setNewViewportSize(
                        Size(
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (state.controllingStick.isInside(offset)) {
                                isStickActive = true
                                val angle = state.controllingStick.getTouchAngle(offset)
                                viewModel.setDirectionAngle(angle)
                                currentStickInputAngle = angle
                                currentStickInputDistanceFactor =
                                    state.controllingStick.getDistanceFactor(offset)
                            } else {
                                isStickActive = false
                                currentStickInputAngle = null
                                currentStickInputDistanceFactor = 0f
                            }
                        },
                        onDrag = { change, _ ->
                            if (isStickActive) {
                                val angle = state.controllingStick.getTouchAngle(change.position)
                                viewModel.setDirectionAngle(angle)
                                currentStickInputAngle = angle
                                currentStickInputDistanceFactor =
                                    state.controllingStick.getDistanceFactor(change.position)
                            }
                        },
                        onDragEnd = {
                            if (isStickActive) {
                                viewModel.setDirectionAngle(null)
                                isStickActive = false
                                currentStickInputAngle = null
                                currentStickInputDistanceFactor = 0f
                            }
                        },
                        onDragCancel = {
                            if (isStickActive) {
                                viewModel.setDirectionAngle(null)
                                isStickActive = false
                                currentStickInputAngle = null
                                currentStickInputDistanceFactor = 0f
                            }
                        }
                    )
                }
        ) {
            drawBackgroundTexture(
                state.gameMap,
                state.gameCamera,
                bitmaps["terrain_500"]!!
            )

            drawGameMap(
                state.gameMap,
                state.gameCamera,
                state.gameCamera.viewportSize,
                bitmaps
            )

            drawCars(state, bitmaps)

            drawControllingStick(
                state.controllingStick,
                currentStickInputAngle,
                currentStickInputDistanceFactor
            )

            drawMinimap(state.gameMap, state.mainPlayer.car, state.checkpointManager)

            drawNextCheckpoint(
                state.checkpointManager.getNextCheckpoint(state.mainPlayer.car.id),
                state.gameCamera,
                state.gameCamera.getScaledCellSize(state.gameMap.size)
            )
        }

        Text(
            text = "Lap: ${state.lapsCompleted + 1} / 1",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        )

        ModernBackButton(
            onClick = {
                viewModel.disconnect()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )
    }
}