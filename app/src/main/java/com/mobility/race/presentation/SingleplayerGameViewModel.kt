package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.domain.MainLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleplayerGameViewModel : ViewModel(), IGameplay {
    private var directionAngle: Float? = null
    private var gameCycle: Job? = null
    private var isGameRunning = true
    private var logic = MainLogic(this)
    private lateinit var car: Car
    private lateinit var gameMap: GameMap
    private lateinit var camera: GameCamera

    override fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        car = playerCar
        gameMap = playerGameMap
        camera = playerCamera
    }

    override fun runGame() {
        var lastTime = System.currentTimeMillis()
        var currentTime: Long
        var elapsedTime: Float

        gameCycle = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                while (isGameRunning) {
                    currentTime = System.currentTimeMillis()
                    elapsedTime = (currentTime - lastTime) / 1000f

                    if (elapsedTime < 0.016f) {
                        delay(1)
                        continue
                    }

                    logic.handleGameCycle(elapsedTime)
                    lastTime = currentTime
                }
            }
        }
    }

    override fun movePlayer(elapsedTime: Float) {
        car.update(elapsedTime, directionAngle, gameMap)
    }

    override fun moveCamera(elapsedTime: Float) {
        camera.update(elapsedTime)
    }

    override fun setDirectionAngle(angle: Float?) {
        directionAngle = angle
    }

    override fun stopGame() {
        gameCycle?.cancel()
    }
}