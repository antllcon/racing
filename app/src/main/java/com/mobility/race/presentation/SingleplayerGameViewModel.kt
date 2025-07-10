package com.mobility.race.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleplayerGameViewModel : ViewModel(), IGameplay {
    private val touchPosition = mutableStateOf<Offset?>(null)
    private var gameCycle: Job? = null
    private var isGameRunning = true
    private lateinit var car: Car
    private lateinit var gameMap: GameMap
    private lateinit var camera: GameCamera

    override fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        car = playerCar
        gameMap = playerGameMap
        camera = playerCamera
    }

    override fun runGame() {
        val targetFps = 60
        val frameTime = 1000L / targetFps
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                while (isGameRunning) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastTime).coerceAtMost(50) / 1000f
                    lastTime = currentTime

                    // Ограничиваем частоту обновления физики
                    if (deltaTime > 0) {
                        val cellX = car.position.x.toInt().coerceIn(0, gameMap.size - 1)
                        val cellY = car.position.y.toInt().coerceIn(0, gameMap.size - 1)
                        car.setSpeedModifier(gameMap.getSpeedModifier(cellX, cellY))

                        if (touchPosition != null) {
                            car.accelerate(deltaTime)
                        } else {
                            car.decelerate(deltaTime)
                        }

                        car.update(deltaTime)
                        camera.update(deltaTime)
                    }

                    val sleepTime = frameTime - (System.currentTimeMillis() - lastTime)
                    if (sleepTime > 0) {
                        delay(sleepTime) // Теперь это допустимо, так как мы внутри корутины
                    }
                }
            }
         }
    }

    override fun stopGame() {
        gameCycle?.cancel()
    }
}