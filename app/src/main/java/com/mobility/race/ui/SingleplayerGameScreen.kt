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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobility.race.domain.Car
import com.mobility.race.presentation.singleplayer.SingleplayerGameViewModel
import com.mobility.race.ui.drawUtils.bitmapStorage
import com.mobility.race.ui.drawUtils.drawControllingStick
import com.mobility.race.ui.drawUtils.drawImageBitmap
import com.mobility.race.ui.drawUtils.drawGameMap
import kotlin.math.PI
import kotlin.math.min

@Composable
fun SingleplayerGameScreen(viewModel: SingleplayerGameViewModel = viewModel()) {
    val state = viewModel.state.value
    val bitmaps = bitmapStorage()
    var minimapSize by remember { mutableStateOf(Size(300f, 300f)) }
    val minimapPosition = remember { Offset(30f, 30f) }
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
        )
        {
            // TODO: убрать
            val (_, zoom) = state.gameCamera.getViewMatrix()
            val baseCellSize = min(size.width, size.height) / state.gameMap.size.toFloat()
            val scaledCellSize = baseCellSize * zoom

            drawGameMap(state.gameMap, state.gameCamera, size, bitmaps)
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.7f),
                topLeft = minimapPosition,
                size = minimapSize,
                cornerRadius = CornerRadius(8f)
            )
            val cellSize = min(
                minimapSize.width / state.gameMap.width,
                minimapSize.height / state.gameMap.height
            )
            for (y in 0 until state.gameMap.height) {
                for (x in 0 until state.gameMap.width) {
                    val terrain = state.gameMap.getTerrainType(x, y)
                    val color = when (terrain.uppercase()) {
                        "GRASS" -> Color(0xFF4CAF50)
                        "ABYSS" -> Color(0xFF2196F3)
                        "ROAD" -> Color(0xFF795548)
                        else -> {
                            Color.Gray
                        }
                    }

                    drawRect(
                        color = color,
                        topLeft = Offset(
                            minimapPosition.x + x * cellSize,
                            minimapPosition.y + y * cellSize
                        ),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
            drawCircle(
                color = Color.Blue,
                center = Offset(
                    minimapPosition.x + state.car.position.x * cellSize,
                    minimapPosition.y + state.car.position.y * cellSize
                ),
                radius = cellSize * 0.7f
            )

            drawControllingStick(
                state.controllingStick,
                currentStickInputAngle,
                currentStickInputDistanceFactor
            )


            val playerScreenPos = state.gameCamera.worldToScreen(state.car.position)
            rotate(
                degrees = state.car.visualDirection * (180f / PI.toFloat()) + 90,
                pivot = playerScreenPos
            ) {
                val carWidthPx = Car.WIDTH * scaledCellSize
                val carLengthPx = Car.LENGTH * scaledCellSize

                drawImageBitmap(
                    bitmaps["car" + state.car.id + "_" + state.car.currentSprite]!!,
                    Offset(playerScreenPos.x - carLengthPx / 2, playerScreenPos.y - carWidthPx / 2),
                    Size(carLengthPx, carWidthPx)
                )
            }
        }
    }
}
