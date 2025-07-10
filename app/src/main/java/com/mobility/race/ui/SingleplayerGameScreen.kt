package com.mobility.race.ui

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
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
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.IGameplay
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleplayerGameScreen(viewModel: IGameplay) {
    val car = remember {
        Car("Player", initialPosition = Offset(5f, 5f))
    }

    val gameMap = remember { GameMap.createRaceTrackMap() }
    val density = LocalDensity.current

    var gameTime by remember { mutableLongStateOf(0L) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }

    var viewportSize by remember { mutableStateOf(Size.Zero) }
    val camera = remember {
        GameCamera(
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
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        touchPosition = null
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
        ) {
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

            touchPosition?.let { touchPos ->
                val worldTouchPos = camera.screenToWorld(touchPos)
                val angle = atan2(
                    worldTouchPos.y - car.position.y,
                    worldTouchPos.x - car.position.x
                )
                var diff = angle - car.direction

                // Нормализуем угол
                while (diff > PI) diff -= 2 * PI.toFloat()
                while (diff < -PI) diff += 2 * PI.toFloat()

                // Если касание сзади (угол > 90 градусов), игнорируем
                if (abs(diff) < PI.toFloat() / 2) {
                    car.startTurn(if (diff > 0) 1f else -1f)
                } else {
                    car.stopTurn()
                }

                drawCircle(
                    Color.Blue.copy(alpha = 0.5f),
                    radius = 20f,
                    center = touchPos
                )
            }
        }
    }
}