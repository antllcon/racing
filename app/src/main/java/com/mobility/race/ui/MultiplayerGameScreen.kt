package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.ui.drawUtils.bitmapStorage
import com.mobility.race.ui.drawUtils.drawBackgroundTexture
import com.mobility.race.ui.drawUtils.drawCars
import com.mobility.race.ui.drawUtils.drawControllingStick
import com.mobility.race.ui.drawUtils.drawGameMap
import com.mobility.race.ui.drawUtils.drawMinimap
import com.mobility.race.ui.drawUtils.drawNextCheckpoint

@Composable
fun MultiplayerGameScreen(
    viewModel: MultiplayerGameViewModel
) {
    val state = viewModel.state.value
    val bitmaps = bitmapStorage()

    var isStickActive by remember { mutableStateOf(false) }
    var currentStickInputAngle: Float? by remember { mutableStateOf(null) }
    var currentStickInputDistanceFactor: Float by remember { mutableFloatStateOf(0f) }

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
                Text(text = (state.countdown.toInt() + 1).toString())
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
    }
}