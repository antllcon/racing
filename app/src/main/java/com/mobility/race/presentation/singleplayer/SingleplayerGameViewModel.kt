package com.mobility.race.presentation.singleplayer

import androidx.lifecycle.viewModelScope
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SingleplayerGameViewModel :
    BaseViewModel<SingleplayerGameState>(SingleplayerGameState.default(((1..6).random().toString()))) {
    private var gameCycle: Job? = null

    init {
        init()
    }

    private fun init() {
        modifyState {
            copy(isGameRunning = true)
        }

        runGame()
    }

    private fun runGame() {
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            while (stateValue.isGameRunning) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = (currentTime - lastTime) / 1000f

                movePlayer(elapsedTime)
                checkCheckpoints()
                moveCamera()
                lastTime = currentTime

                delay(16)
            }
        }
    }

    private fun movePlayer(elapsedTime: Float) {
        val speedModifier = stateValue.gameMap.getSpeedModifier(stateValue.car.position)

        modifyState {
            copy(
                car = car.update(elapsedTime, stateValue.directionAngle, speedModifier),
            )
        }
    }

    private fun checkCheckpoints() {
        val car = stateValue.car
        val manager = stateValue.checkpointManager
        val carId = car.id

        val nextCheckpoint = manager.getNextCheckpoint(carId) ?: return

        val carCellX = car.position.x.toInt()
        val carCellY = car.position.y.toInt()

        if (carCellX == nextCheckpoint.x.toInt() && carCellY == nextCheckpoint.y.toInt()) {
            manager.onCheckpointReached(carId, nextCheckpoint)

            val newLaps = manager.getLapsForCar(carId)
            if (newLaps != stateValue.lapsCompleted) {
                modifyState { copy(lapsCompleted = newLaps) }
                println("Lap ${stateValue.lapsCompleted}/${stateValue.totalLaps} completed!")
            }

            if (stateValue.lapsCompleted >= stateValue.totalLaps) {
                endRace()
            }
        }
    }

    private fun moveCamera() {
        modifyState {
            copy(
                gameCamera = gameCamera.update(car.position)
            )
        }
    }

    fun setDirectionAngle(angle: Float?) {
        modifyState {
            copy(
                directionAngle = angle
            )
        }
    }

    private fun endRace() {
        modifyState { copy(isGameRunning = false) }
        println("Player ${stateValue.car.playerName} finished the race!")
        // Навигация на экран результатов
    }


    fun stopGame() {
        gameCycle?.cancel()
    }
}