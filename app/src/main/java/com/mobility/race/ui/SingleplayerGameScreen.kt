package com.mobility.race.ui

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.mobility.race.domain.Car
import com.mobility.race.domain.Camera
import com.mobility.race.domain.Map
import com.mobility.race.presentation.IGameplay
import kotlin.math.PI
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleplayerGameScreen(viewModel: IGameplay) {
    val car = remember {
        Car("Player", initialPosition = Offset(5f, 5f))
    }

    val gameMap = remember { Map.createRaceTrackMap() }
    val density = LocalDensity.current

    var gameTime by remember { mutableLongStateOf(0L) }
    var touchPosition by remember { mutableStateOf<Offset>(Offset(0f, 0f)) }

    var viewportSize by remember { mutableStateOf(Size.Zero) }
    val camera = remember {
        Camera(
            targetCar = car,
            initialViewportSize = Size.Zero,
            mapSize = gameMap.size
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        touchPosition = Offset(event.x, event.y)
                        viewModel.movePlayer(touchPosition)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        touchPosition = Offset(0f, 0f)
                        viewModel.movePlayer(touchPosition)
                        car.stopTurn()
                        true
                    }

                    else -> false
                }
            }
            .onGloballyPositioned {
                viewModel.init(car, gameMap, camera)
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
        )   {
                if (viewportSize.width <= 0) return@Canvas

                val (cameraPos, zoom) = camera.getViewMatrix()
                val baseCellSize = min(size.width, size.height) / gameMap.size.toFloat()
                val scaledCellSize = baseCellSize * zoom

                for (i in 0 until gameMap.size) {
                    for (j in 0 until gameMap.size) {
                        val worldPos = Offset(j.toFloat(), i.toFloat())
                        val screenPos = camera.worldToScreen(worldPos)

                        val color = when (gameMap.getTerrainAt(i, j)) {
                            Map.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                            Map.TerrainType.GRASS -> Color(0xFF4CAF50)
                            Map.TerrainType.ROAD -> Color(0xFF616161)
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

                val carScreenPos = camera.worldToScreen(car.position)

                rotate(
                    degrees = car.visualDirection * (180f / PI.toFloat()),
                    pivot = carScreenPos
                ) {
                    val carWidthPx = Car.WIDTH * scaledCellSize
                    val carLengthPx = Car.LENGTH * scaledCellSize
                    drawRect(
                        Color.Red,
                        Offset(carScreenPos.x - carLengthPx / 2, carScreenPos.y - carWidthPx / 2),
                        Size(carLengthPx, carWidthPx)
                    )
                }

                drawCircle(
                    Color.Blue.copy(alpha = 0.5f),
                    radius = 30f,
                    center = touchPosition
                )
            }
        }
    }

fun handleTouch(event: MotionEvent) {

}