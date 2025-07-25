package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobility.race.domain.Car
import com.mobility.race.presentation.singleplayer.SingleplayerGameViewModel
import com.mobility.race.ui.drawUtils.bitmapStorage
import com.mobility.race.ui.drawUtils.drawBackgroundTexture
import com.mobility.race.ui.drawUtils.drawControllingStick
import com.mobility.race.ui.drawUtils.drawGameMap
import com.mobility.race.ui.drawUtils.drawMinimap
import com.mobility.race.ui.drawUtils.drawImageBitmap
import com.mobility.race.ui.drawUtils.drawNextCheckpoint
import kotlin.math.PI
import kotlin.math.min
import com.mobility.race.ui.RaceFinishedScreen
@Composable
fun SingleplayerGameScreen(
    viewModel: SingleplayerGameViewModel = viewModel(),
    navigateToFinished: (finishTime: Long, lapsCompleted: Int, totalLaps: Int) -> Unit,
    onExit: () -> Unit = {}
) {
    val state = viewModel.state.value
    val bitmaps = bitmapStorage()

    var isStickActive by remember { mutableStateOf(false) }
    var currentStickInputAngle: Float? by remember { mutableStateOf(null) }
    var currentStickInputDistanceFactor: Float by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
            if (state.isGameRunning) {
                drawBackgroundTexture(
                    state.gameMap,
                    state.gameCamera,
                    bitmaps["terrain_500"]!!
                )

                drawGameMap(
                    state.gameMap,
                    state.gameCamera,
                    size,
                    bitmaps
                )

                drawMinimap(state)

                drawNextCheckpoint(
                    state.checkpointManager.getNextCheckpoint(state.car.id),
                    state.gameCamera,
                    state.gameCamera.getScaledCellSize(state.gameMap.size)
                )

                rotate(
                    degrees = state.car.visualDirection * (180f / PI.toFloat()) + 90,
                    pivot = state.gameCamera.worldToScreen(state.car.position)
                ) {
                    drawImageBitmap(
                        bitmaps["car" + state.car.id + "_" + state.car.currentSprite]!!,
                        Offset(state.gameCamera.worldToScreen(state.car.position).x - Car.LENGTH * state.gameCamera.getScaledCellSize(state.gameMap.size) / 2,
                            state.gameCamera.worldToScreen(state.car.position).y - Car.WIDTH * state.gameCamera.getScaledCellSize(state.gameMap.size) / 2),
                        Size(Car.LENGTH * state.gameCamera.getScaledCellSize(state.gameMap.size), Car.WIDTH * state.gameCamera.getScaledCellSize(state.gameMap.size))
                    )
                }
            }
        }

        if (state.isGameRunning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawControllingStick(
                    state.controllingStick,
                    currentStickInputAngle,
                    currentStickInputDistanceFactor
                )
            }

            Text(
                text = "Lap: ${state.lapsCompleted + 1} / ${state.totalLaps}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            )
        }

        if (!state.isGameRunning) {
            LaunchedEffect(state.finishTime, state.lapsCompleted, state.totalLaps) {
                if (state.finishTime > 0) {
                    navigateToFinished(state.finishTime, state.lapsCompleted, state.totalLaps)
                }
            }
        }
    }
}