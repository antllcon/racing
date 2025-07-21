package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.singleplayer.SingleplayerGameViewModel
import com.mobility.race.ui.drawUtils.drawControllingStick
import kotlin.math.PI
import kotlin.math.min

@Composable
fun SingleplayerGameScreen(viewModel: SingleplayerGameViewModel = viewModel()) {
    val state = viewModel.state.value

    var isStickActive by remember { mutableStateOf(false) }
    var currentStickInputAngle: Float? by remember { mutableStateOf(null) }
    var currentStickInputDistanceFactor: Float by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
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
                                currentStickInputDistanceFactor = state.controllingStick.getDistanceFactor(offset)
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
                                currentStickInputDistanceFactor = state.controllingStick.getDistanceFactor(change.position)
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
            val (_, zoom) = state.gameCamera.getViewMatrix()
            val baseCellSize = min(size.width, size.height) / state.gameMap.size.toFloat()
            val scaledCellSize = baseCellSize * zoom

            // Draw map
            for (i in 0 until state.gameMap.size) {
                for (j in 0 until state.gameMap.size) {
                    val worldPos = Offset(j.toFloat(), i.toFloat())
                    val screenPos = state.gameCamera.worldToScreen(worldPos)

                    val color = when (state.gameMap.getTerrainAt(i, j)) {
                        GameMap.TerrainType.ROAD -> Color(0xFF616161)
                        GameMap.TerrainType.GRASS -> Color(0xFF4CAF50)
                        GameMap.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                    }

                    drawRect(color, screenPos, Size(scaledCellSize, scaledCellSize))
                    drawRect(
                        Color.Black.copy(alpha = 0.3f),
                        screenPos,
                        Size(scaledCellSize, scaledCellSize),
                        style = Stroke(1f)
                    )
                }
            }

            drawControllingStick(state.controllingStick, currentStickInputAngle, currentStickInputDistanceFactor)

            val playerScreenPos = state.gameCamera.worldToScreen(state.car.position)
            rotate(
                degrees = state.car.visualDirection * (180f / PI.toFloat()),
                pivot = playerScreenPos
            ) {
                val carWidthPx = Car.WIDTH * scaledCellSize
                val carLengthPx = Car.LENGTH * scaledCellSize
                drawRect(
                    Color.Red,
                    Offset(playerScreenPos.x - carLengthPx / 2, playerScreenPos.y - carWidthPx / 2),
                    Size(carLengthPx, carWidthPx)
                )
            }
        }
    }
}