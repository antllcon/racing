package com.mobility.race.presentation.singleplayer

import androidx.lifecycle.viewModelScope
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleplayerGameViewModel :
    BaseViewModel<SingleplayerGameState>(SingleplayerGameState.default()) {
    private var gameCycle: Job? = null

    init {
        init()
    }

    fun init() {
        modifyState {
            copy(isGameRunning = true)
        }

        runGame()
    }

    fun runGame() {
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            while (stateValue.isGameRunning) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = (currentTime - lastTime) / 1000f

                movePlayer(elapsedTime)
                moveCamera(elapsedTime)

                lastTime = currentTime
                delay(16)
            }
        }
    }

    private fun movePlayer(elapsedTime: Float) {
        modifyState {
            copy(
                car = car.update(elapsedTime, stateValue.directionAngle, stateValue.gameMap),
            )
        }
    }

    private fun moveCamera(elapsedTime: Float) {
        modifyState {
            copy(
                gameCamera = gameCamera.update(elapsedTime)
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

    fun stopGame() {
        gameCycle?.cancel()
    }
}