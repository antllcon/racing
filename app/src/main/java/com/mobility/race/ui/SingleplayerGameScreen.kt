package com.mobility.race.ui

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.IGameplay
import com.mobility.race.ui.drawUtils.drawControllingStick
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleplayerGameScreen(viewModel: IGameplay) {
    val playerCar = remember {
        Car("Player", initialPosition = Offset(5f, 5f))
    }

    val gameMap = remember { GameMap.createRaceTrackMap() }

    var viewportSize by remember { mutableStateOf(Size.Zero) }
    val camera = remember {
        GameCamera(
            targetCar = playerCar,
            initialViewportSize = Size.Zero,
            mapSize = gameMap.size
        )
    }
    val controllingStick = remember { ControllingStick(Resources.getSystem().getDisplayMetrics().widthPixels) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .onGloballyPositioned {
                viewModel.init(playerCar, gameMap, camera)
                viewModel.runGame()
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    viewportSize = Size(size.width.toFloat(), size.height.toFloat())
                    camera.setViewportSize(viewportSize)
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        if (controllingStick.isDragInsideStick(change.position)) {
                            
                        }
                    }
                }
        )   {
            if (viewportSize.width <= 0) return@Canvas

            val (cameraPos, zoom) = camera.getViewMatrix()
            val baseCellSize = min(size.width, size.height) / gameMap.size.toFloat()
            val scaledCellSize = baseCellSize * zoom

            // Draw map
            for (i in 0 until gameMap.size) {
                for (j in 0 until gameMap.size) {
                    val worldPos = Offset(j.toFloat(), i.toFloat())
                    val screenPos = camera.worldToScreen(worldPos)

                    val color = when (gameMap.getTerrainAt(i, j)) {
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

            drawControllingStick(controllingStick)

            val playerScreenPos = camera.worldToScreen(playerCar.position)
            rotate(
                degrees = playerCar.visualDirection * (180f / PI.toFloat()),
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

private fun handleCollision(car1: Car, car2: Car) {
    val direction = atan2(
        car2.position.y - car1.position.y,
        car2.position.x - car1.position.x
    )

    val moveDistance = 0.05f // в будущем переделать на зависимость от скорости
    car1.position = Offset(
        car1.position.x - cos(direction) * moveDistance,
        car1.position.y - sin(direction) * moveDistance
    )
    car2.position = Offset(
        car2.position.x + cos(direction) * moveDistance,
        car2.position.y + sin(direction) * moveDistance
    )
}