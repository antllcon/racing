package com.mobility.race.presentation.singleplayer

import androidx.lifecycle.viewModelScope
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SingleplayerGameViewModel :
    BaseViewModel<SingleplayerGameState>(SingleplayerGameState.default(((1..6).random().toString()))) {

    private var gameCycle: Job? = null
    private var carId: String = ((1..6).random().toString())

    init {
        startNewGame()
    }

    fun startNewGame() {
        carId = ((1..6).random().toString())

        gameCycle?.cancel()

        modifyState {
            SingleplayerGameState.default(carId).copy(
                isGameRunning = true,
                startTime = System.currentTimeMillis(),
                finishTime = 0L,
                lapsCompleted = 0
            )
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
        modifyState {
            copy(
                isGameRunning = false,
                finishTime = System.currentTimeMillis() - startTime
            )
        }
        gameCycle?.cancel()
    }

    fun restartGame() {
        startNewGame()
    }

    override fun onCleared() {
        super.onCleared()
        gameCycle?.cancel()
    }
}