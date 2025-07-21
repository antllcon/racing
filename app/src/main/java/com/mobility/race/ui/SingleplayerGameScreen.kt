package com.mobility.race.ui

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.singleplayer.SingleplayerGameViewModel
import com.mobility.race.ui.drawUtils.drawControllingStick
import kotlin.math.PI
import kotlin.math.min

@Composable
fun SingleplayerGameScreen(viewModel: SingleplayerGameViewModel) {
    val state = viewModel.state.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .onGloballyPositioned {
                if (!state.isGameRunning) {
                    viewModel.init()
                    viewModel.runGame()
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    state.controllingStick.setMinScreenSize(Resources.getSystem().displayMetrics.widthPixels)
                    state.gameCamera.setNewViewportSize(
                        Size(
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            if (state.controllingStick.isTouchInsideStick(change.position)) {
                                viewModel.setDirectionAngle(
                                    state.controllingStick.getTouchAngle(
                                        change.position
                                    )
                                )
                            } else {
                                viewModel.setDirectionAngle(null)
                            }
                        },
                        onDragEnd = {
                            viewModel.setDirectionAngle(null)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (state.controllingStick.isTouchInsideStick(offset)) {
                                viewModel.setDirectionAngle(
                                    state.controllingStick.getTouchAngle(
                                        offset
                                    )
                                )
                            }
                            if (tryAwaitRelease()) {
                                viewModel.setDirectionAngle(null)
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
                        GameMap.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                        GameMap.TerrainType.GRASS -> Color(0xFF4CAF50)
                        GameMap.TerrainType.ROAD -> Color(0xFF616161)
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

            drawControllingStick(state.controllingStick)

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

//private fun handleCollision(car1: Car, car2: Car) {
//    val direction = atan2(
//        car2.position.y - car1.position.y,
//        car2.position.x - car1.position.x
//    )
//
//    val moveDistance = 0.05f // в будущем переделать на зависимость от скорости
//    car1.position = Offset(
//        car1.position.x - cos(direction) * moveDistance,
//        car1.position.y - sin(direction) * moveDistance
//    )
//    car2.position = Offset(
//        car2.position.x + cos(direction) * moveDistance,
//        car2.position.y + sin(direction) * moveDistance
//    )
//}