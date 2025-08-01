package com.mobility.race.presentation.singleplayer

import SoundManager
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SingleplayerGameViewModel(private val context: Context) :
    BaseViewModel<SingleplayerGameState>(SingleplayerGameState.default(((1..6).random().toString()))) {

    private var gameCycle: Job? = null
    private var carId: String = ((1..6).random().toString())
    lateinit var soundManager: SoundManager
    private var previousSpeed: Float = 0f
    private var currentSurface: String = "ROAD"
    private var lastSurfaceUpdateTime = 0L
    private val surfaceUpdateInterval = 100L

    init {
        startNewGame()
    }


    fun startNewGame() {
        carId = ((1..6).random().toString())

        try {
            soundManager = SoundManager(context)
            soundManager.playBackgroundMusic()
        } catch (e: Exception) {
            modifyState {
                copy(isGameRunning = false)
            }
            return
        }
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
                stateValue.gameMap.updateBonuses(currentTime)

                val expired = stateValue.activeBonuses.filter { it.value <= currentTime }.keys
                if (expired.isNotEmpty()) {
                    modifyState {
                        copy(activeBonuses = activeBonuses - expired)
                    }
                }

                lastTime = currentTime
                delay(16)
            }
        }
    }

    private fun getSpeedModifier(): Float {
        val baseModifier = stateValue.gameMap.getSpeedModifier(stateValue.car.position)
        val bonusModifier = if (stateValue.activeBonuses.containsKey("bonus_speed")) 1.5f else 1f
        return baseModifier * bonusModifier
    }

    private fun movePlayer(elapsedTime: Float) {
        val speedModifier = getSpeedModifier()
        val hasSizeBonus = stateValue.activeBonuses.containsKey(GameMap.Bonus.TYPE_SIZE)

        val carPos = stateValue.car.position
        val surfaceType = stateValue.gameMap.getTerrainType(carPos.x.toInt(), carPos.y.toInt())

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSurfaceUpdateTime > surfaceUpdateInterval) {
            updateSurfaceSound(surfaceType, stateValue.car.speed)
            lastSurfaceUpdateTime = currentTime
        }

        modifyState {
            copy(
                car = car.update(
                    elapsedTime = elapsedTime,
                    directionAngle = directionAngle,
                    speedModifier = speedModifier,
                    hasSizeBonus = hasSizeBonus
                )
            )
        }

        val currentSpeed = stateValue.car.speed

        if (previousSpeed <= Car.MIN_SPEED && currentSpeed > Car.MIN_SPEED) {
            soundManager.playStartSound()
        }
        checkBonuses()
        previousSpeed = currentSpeed
    }

    private fun checkBonuses() {
        val car = stateValue.car
        val carCellX = car.position.x.toInt()
        val carCellY = car.position.y.toInt()
        val currentTime = System.currentTimeMillis()

        stateValue.gameMap.getBonuses().forEach { bonus ->
            if (bonus.isActive &&
                carCellX == bonus.position.x.toInt() &&
                carCellY == bonus.position.y.toInt()
            ) {

                bonus.isActive = false
                bonus.spawnTime = currentTime
                applyBonus(bonus.type)
            }
        }
    }

    private fun applyBonus(type: String) {
        val duration = when (type) {
            GameMap.Bonus.TYPE_SPEED -> 5000L
            GameMap.Bonus.TYPE_SIZE -> 7000L
            else -> 5000L
        }

        modifyState {
            copy(
                activeBonuses = activeBonuses + (type to (System.currentTimeMillis() + duration))
            )
        }

        soundManager.playBonusSound()
    }

    private fun updateSurfaceSound(surfaceType: String, carSpeed: Float) {
        if (surfaceType != currentSurface) {
            currentSurface = surfaceType
            soundManager.playSurfaceSound(surfaceType, calculateSurfaceVolume(carSpeed))
        } else {
            soundManager.updateSurfaceSoundVolume(calculateSurfaceVolume(carSpeed))
        }
    }

    private fun calculateSurfaceVolume(carSpeed: Float): Float {
        return 0.1f + (carSpeed / Car.MAX_SPEED) * 0.9f
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
                modifyState {
                    copy(lapsCompleted = newLaps)
                }
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
                isRaceFinished = true,
                finishTime = System.currentTimeMillis(),
                raceTime = System.currentTimeMillis() - startTime
            )
        }
        gameCycle?.cancel()
        soundManager.stopSurfaceSound()
        soundManager.pauseBackgroundMusic()
    }

    fun restartGame() {
        startNewGame()
    }

    override fun onCleared() {
        super.onCleared()
        gameCycle?.cancel()
        soundManager.stopSurfaceSound()
        soundManager.release()
    }
}