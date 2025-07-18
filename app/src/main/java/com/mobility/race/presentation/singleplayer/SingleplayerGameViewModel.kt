package com.mobility.race.presentation.singleplayer

import androidx.lifecycle.viewModelScope
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleplayerGameViewModel : BaseViewModel<SingleplayerGameState>(SingleplayerGameState.default()) {
    private var gameCycle: Job? = null

    fun init() {
        stateValue.isGameRunning = true
    }

    fun runGame() {
        var lastTime = System.currentTimeMillis()
        var currentTime: Long
        var elapsedTime: Float

        gameCycle = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                while (stateValue.isGameRunning) {
                    currentTime = System.currentTimeMillis()
                    elapsedTime = (currentTime - lastTime) / 1000f

                    movePlayer(elapsedTime)
                    moveCamera(elapsedTime)
                    lastTime = currentTime

                    delay(16)
                }
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
            copy (
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