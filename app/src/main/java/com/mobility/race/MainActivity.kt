package com.mobility.race

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameMap
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.runtime.withFrameMillis
import kotlin.math.abs

fun Offset.getDistance(): Float {
    return kotlin.math.sqrt(x * x + y * y)
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarGameScreen()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarGameScreen() {
    val car = remember {
        Car("Player").apply {
            position = Offset(5f, 5f)
        }
    }

    val gameMap = remember { GameMap.createRaceTrackMap() }
    var gameTime by remember { mutableLongStateOf(0L) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                val deltaTime = (time - gameTime).coerceAtMost(50) / 1000f
                gameTime = time

                val cellX = car.position.x.toInt().coerceIn(0, 9)
                val cellY = car.position.y.toInt().coerceIn(0, 9)

                car.setSpeedModifier(gameMap.getSpeedModifier(cellX, cellY))

                // Управление из примера
                if (touchPosition != null) {
                    car.accelerate(deltaTime)
                }

                car.update(deltaTime)
            }
        }
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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.minDimension / 10f
            val offsetX = (size.width - 10 * cellSize) / 2
            val offsetY = (size.height - 10 * cellSize) / 2

            // Обработка касания как в примере
            touchPosition?.let { touchPos ->
                val gameX = (touchPos.x - offsetX) / cellSize
                val gameY = (touchPos.y - offsetY) / cellSize
                val targetPos = Offset(gameX, gameY)

                val angle = atan2(
                    targetPos.y - car.position.y,
                    targetPos.x - car.position.x
                )
                val currentAngle = car.getDirection()
                var diff = angle - currentAngle

                // Нормализация угла
                while (diff > PI) diff -= 2 * PI.toFloat()
                while (diff < -PI) diff += 2 * PI.toFloat()

                car.startTurn(if (diff > 0) 1f else -1f)
            }

            // Draw map (осталось как у вас)
            for (i in 0 until 10) {
                for (j in 0 until 10) {
                    val cellPos = Offset(offsetX + j * cellSize, offsetY + i * cellSize)
                    val color = when (gameMap.getTerrainAt(i, j)) {
                        GameMap.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                        GameMap.TerrainType.GRASS -> Color(0xFF4CAF50)
                        GameMap.TerrainType.ROAD -> Color(0xFF616161)
                    }

                    drawRect(color, cellPos, Size(cellSize, cellSize))
                    drawRect(
                        Color.Black.copy(alpha = 0.3f),
                        cellPos,
                        Size(cellSize, cellSize),
                        style = Stroke(1f)
                    )
                }
            }

            // Draw car (осталось как у вас)
            val carScreenPos = Offset(
                offsetX + car.position.x * cellSize,
                offsetY + car.position.y * cellSize
            )

            rotate(
                degrees = car.getVisualDirection() * (180f / PI.toFloat()),
                pivot = carScreenPos
            ) {
                drawRect(
                    Color.Red,
                    Offset(carScreenPos.x - Car.SIZE / 2, carScreenPos.y - Car.SIZE / 2),
                    Size(Car.SIZE, Car.SIZE)
                )
            }

            // Draw target (опционально)
            touchPosition?.let { touchPos ->
                drawCircle(
                    color = Color.Blue.copy(alpha = 0.5f),
                    radius = 20f,
                    center = touchPos
                )
            }
        }
    }
}