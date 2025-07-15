package com.mobility.race.ui

import android.content.res.Resources
import android.util.DisplayMetrics
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.domain.Car
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.IGameplay
import com.mobility.race.ui.drawUtils.drawControllingStick
import kotlin.math.PI
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleplayerGameScreen(viewModel: IGameplay) {
    val car = remember {
        Car("Player", initialPosition = Offset(5f, 5f))
    }

    val gameMap = remember { GameMap.createRaceTrackMap() }

    var viewportSize by remember { mutableStateOf(Size.Zero) }
    val camera = remember {
        GameCamera(
            targetCar = car,
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
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        if (controllingStick.isDragInsideStick(change.position)) {
                            viewModel.movePlayer(change.position)
                        }
                    }
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

                val carScreenPos = camera.worldToScreen(car.position)
                val carSizePx = Car.SIZE * scaledCellSize

                rotate(
                    degrees = car.visualDirection * (180f / PI.toFloat()),
                    pivot = carScreenPos
                ) {
                    drawRect(
                        Color.Red,
                        Offset(carScreenPos.x - carSizePx / 2, carScreenPos.y - carSizePx / 2),
                        Size(carSizePx, carSizePx)
                    )
                }
            }
        }
    }