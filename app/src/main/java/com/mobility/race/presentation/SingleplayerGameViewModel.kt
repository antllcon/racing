package com.mobility.race.presentation

import androidx.compose.runtime.mutableStateOf
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class SingleplayerGameViewModel : ViewModel(), IGameplay {
    private var touchPosition: Offset? = null
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
                    elapsedTime = (currentTime - lastTime).toFloat()

                    logic.handleGameCycle(elapsedTime)

                    lastTime = currentTime
                }
            }
        }
    }

    override fun movePlayer(elapsedTime: Float) {
        car.update(elapsedTime)

        if (elapsedTime > 0) {
            val cellX = car.position.x.toInt().coerceIn(0, gameMap.size - 1)
            val cellY = car.position.y.toInt().coerceIn(0, gameMap.size - 1)
            car.setSpeedModifier(gameMap.getSpeedModifier(cellX, cellY))

            if (touchPosition != null) {
                car.accelerate(elapsedTime)
            } else {
                car.decelerate(elapsedTime)
            }
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
            car.startTurn(if (diff > 0) 1f else -1f)
        }
    }

    override fun moveCamera(elapsedTime: Float) {
        camera.update(elapsedTime)
    }

    override fun setTouchPosition(newTouchPosition: Offset?) {
        touchPosition = newTouchPosition
    }

    override fun stopGame() {
        gameCycle?.cancel()
    }
}