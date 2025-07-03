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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.mobility.race.domain.Car
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarGameScreen()
        }
    }
}
//check car class
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarGameScreen() {
    val screenWidth = with(LocalDensity.current) { 1000.dp.toPx() }
    val screenHeight = with(LocalDensity.current) { 1000.dp.toPx() }

    val car = remember {
        Car("Player").apply {
            position = Offset(screenWidth/2, screenHeight/2)
        }
    }

    var gameTime by remember { mutableStateOf(0L) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                val deltaTime = (time - gameTime).coerceAtMost(50) / 1000f
                gameTime = time

                car.accelerate(deltaTime)

                if (touchPosition != Offset.Zero) {
                    val angle = atan2(
                        touchPosition.y - car.position.y,
                        touchPosition.x - car.position.x
                    )
                    val currentAngle = car.getDirection()
                    var diff = angle - currentAngle

                    while (diff > PI) diff -= 2 * PI.toFloat()
                    while (diff < -PI) diff += 2 * PI.toFloat()

                    car.startTurn(if (diff > 0) 1f else -1f)
                } else {
                    car.stopTurn()
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
                        touchPosition = Offset.Zero
                        true
                    }
                    else -> false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(
                degrees = car.getVisualDirection() * (180f / PI.toFloat()),
                pivot = car.position
            ) {
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(
                        car.position.x - Car.SIZE/2,
                        car.position.y - Car.SIZE/2
                    ),
                    size = androidx.compose.ui.geometry.Size(Car.SIZE, Car.SIZE)
                )
            }

            if (touchPosition != Offset.Zero) {
                drawCircle(
                    color = Color.Blue,
                    center = touchPosition,
                    radius = 20f
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarGamePreview() {
    CarGameScreen()
}