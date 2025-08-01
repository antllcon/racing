package com.mobility.race.presentation.multiplayer

import SoundManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.BonusPickedUpResponse
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.GameStopResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.PlayerDisconnectedResponse
import com.mobility.race.data.PlayerInputRequest
import com.mobility.race.data.PlayerResultStorage
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.Bonus
import com.mobility.race.domain.Car
import com.mobility.race.presentation.BaseViewModel
import com.mobility.race.ui.PlayerResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.PI

class MultiplayerGameViewModel(
    playerId: String,
    playerNames: Array<String>,
    carSpriteId: String,
    private val context: Context,
    private val gateway: IGateway
) : BaseViewModel<MultiplayerGameState>(
    MultiplayerGameState.default(
        name = playerId,
        playerNames = playerNames,
        carSpriteId = carSpriteId,
        starterPack = gateway.openGatewayStorage()
    )
) {
    private var currentActivePlayerId =
        stateValue.players.indexOfFirst { it == stateValue.mainPlayer }
    private var gameCycle: Job? = null
    private var elapsedTime: Float = 0f
    private val TAG = "MultiplayerGameViewModel"
    var soundManager: SoundManager
    var onFinish: () -> Unit = {}
    var onError: () -> Unit = {}

    private val targetPlayerPositions = mutableMapOf<String, Offset>()
    private val targetPlayerDirections = mutableMapOf<String, Float>()
    private val targetPlayerSpeeds = mutableMapOf<String, Float>()

    private val INTERPOLATION_FACTOR = 0.15f
    private val POSITION_TOLERANCE = 0.5f
    private val CORRECTION_SPEED = 1.0f

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)

        soundManager = SoundManager(context)
        soundManager.playBackgroundMusic()

        startGame()
    }

    fun setDirectionAngle(newAngle: Float?) {
        modifyState {
            copy(
                directionAngle = newAngle
            )
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            gateway.disconnect()
        }
    }

    fun getCars(): Map<Car, Boolean> {
        var cars = emptyMap<Car, Boolean>()

        stateValue.players.forEach {
            cars = cars.plus(Pair(it.car, it.isFinished))
        }

        return cars
    }

    private fun startGame() {
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            while (stateValue.isGameRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = (currentTime - lastTime) / 1000f

                movePlayers(elapsedTime)
                if (stateValue.players.isNotEmpty()) {
                    moveCamera()
                }

                if (!stateValue.mainPlayer.isFinished) {
                    checkCheckpoints()
                    sendPlayerInput()
                }

                lastTime = currentTime
                delay(16)
            }
        }
    }

    private fun movePlayers(elapsedTime: Float) {
        val newPlayersList = stateValue.players.map { player ->
            var updatedCar = player.car
            val speedModifier = stateValue.gameMap.getSpeedModifier(position = player.car.position)

            if (player.car.playerName == stateValue.mainPlayer.car.playerName && !player.isFinished) {
                updatedCar = player.car.update(
                    elapsedTime = elapsedTime,
                    directionAngle = stateValue.directionAngle,
                    speedModifier = speedModifier
                )
            }

            // Интерполяция к серверному состоянию для всех машин
            val targetPosition = targetPlayerPositions[player.car.playerName]
            val targetDirection = targetPlayerDirections[player.car.playerName]
            val targetSpeed = targetPlayerSpeeds[player.car.playerName]

            if (targetPosition != null && targetDirection != null && targetSpeed != null) {
                // Интерполируем позицию
                val interpolatedX = updatedCar.position.x + (targetPosition.x - updatedCar.position.x) * INTERPOLATION_FACTOR
                val interpolatedY = updatedCar.position.y + (targetPosition.y - updatedCar.position.y) * INTERPOLATION_FACTOR
                val newPosition = Offset(interpolatedX, interpolatedY)

                // Интерполируем угол для плавного поворота
                var angleDiff = targetDirection - updatedCar.visualDirection
                while (angleDiff <= -PI) angleDiff += (2 * PI).toFloat()
                while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()
                val newVisualDirection = updatedCar.visualDirection + angleDiff * INTERPOLATION_FACTOR

                // Интерполируем скорость
                val newSpeed = updatedCar.speed + (targetSpeed - updatedCar.speed) * INTERPOLATION_FACTOR

                val newSizeModifier = updatedCar.sizeModifier + (updatedCar.targetSizeModifier - updatedCar.sizeModifier) * INTERPOLATION_FACTOR

                updatedCar = updatedCar.copy(
                    position = newPosition,
                    visualDirection = newVisualDirection,
                    speed = newSpeed,
                    sizeModifier = newSizeModifier
                )
            }

            player.copy(car = updatedCar)
        }

        // Находим обновленного mainPlayer для камеры и других систем
        val newMainPlayer = newPlayersList.find { it.car.playerName == stateValue.mainPlayer.car.playerName } ?: stateValue.mainPlayer

        modifyState {
            copy(
                players = newPlayersList,
                mainPlayer = newMainPlayer
            )
        }
    }

    private suspend fun checkCheckpoints() {
        val car = stateValue.mainPlayer.car
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

            if (stateValue.lapsCompleted >= 3) {
                val newMainPlayer = stateValue.mainPlayer.copy(isFinished = true)
                val newPlayers = stateValue.players.map { player ->
                    if (player.car.playerName == stateValue.mainPlayer.car.playerName) newMainPlayer else player
                }

                modifyState {
                    copy(
                        mainPlayer = newMainPlayer,
                        players = newPlayers,
                    )
                }
                gateway.playerFinished(stateValue.mainPlayer.car.playerName)
            }
        }
    }

    private fun moveCamera() {
        updateActivePlayerId()

        modifyState {
            copy(
                gameCamera = gameCamera.update(stateValue.players[currentActivePlayerId].car.position)
            )
        }
    }


    private fun updateActivePlayerId() {
        if (currentActivePlayerId >= stateValue.players.size || stateValue.players[currentActivePlayerId].isFinished) {
            for (i in 0 until stateValue.players.size) {
                if (!stateValue.players[i].isFinished) {
                    currentActivePlayerId = i
                    break
                }
            }
        }
    }

    private suspend fun sendPlayerInput() {
        val playerInput = PlayerInputRequest(
            visualDirection = stateValue.directionAngle ?: 0f,
            elapsedTime = elapsedTime,
            ringsCrossed = stateValue.checkpointManager.getLapsForCar(stateValue.mainPlayer.car.id)
        )
        gateway.playerAction(playerInput)
    }

    private suspend fun handleMessage(message: ServerMessage) {
        when (message) {
            is ErrorResponse -> {
                Toast.makeText(context, message.message, Toast.LENGTH_SHORT).show()
                onError()
            }

            is PlayerDisconnectedResponse -> {
                var newPlayersList = emptyList<Player>()

                for (player in stateValue.players) {
                    if (player.car.playerName != message.playerId) {
                        newPlayersList = newPlayersList.plus(player)
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersList
                    )
                }

                updateActivePlayerId()
            }

            is GameCountdownUpdateResponse -> {
                modifyState {
                    copy(
                        countdown = message.remainingTime
                    )
                }
            }

            is GameStateUpdateResponse -> {

                message.players.forEach { playerDto ->
                    targetPlayerPositions[playerDto.id] = Offset(playerDto.posX, playerDto.posY)
                    targetPlayerDirections[playerDto.id] = playerDto.visualDirection
                    targetPlayerSpeeds[playerDto.id] = playerDto.speed

                    val playerIndex = stateValue.players.indexOfFirst { it.car.playerName == playerDto.id }
                    if (playerIndex != -1) {
                        val playersCopy = stateValue.players.toMutableList()
                        val existingPlayer = playersCopy[playerIndex]
                        playersCopy[playerIndex] = existingPlayer.copy(
                            isFinished = playerDto.isFinished,
                            car = existingPlayer.car.copy(
                                targetSizeModifier = playerDto.sizeModifier
                            )
                        )
                        modifyState { copy(players = playersCopy) }
                    }
                }

                val newBonuses = message.bonuses.map { dto ->
                    val type = when (dto.type) {
                        "SPEED_BOOST" -> Bonus.BonusType.SPEED_BOOST
                        "MASS_INCREASE" -> Bonus.BonusType.MASS_INCREASE
                        else -> Bonus.BonusType.SPEED_BOOST
                    }

                    when(type) {
                        Bonus.BonusType.SPEED_BOOST -> Bonus.SpeedBoost(
                            position = Offset(dto.posX, dto.posY),
                            isActive = dto.isActive
                        )
                        Bonus.BonusType.MASS_INCREASE -> Bonus.MassIncrease(
                            position = Offset(dto.posX, dto.posY),
                            isActive = dto.isActive
                        )
                    }
                }

                modifyState { copy(bonuses = newBonuses) }
            }

            is BonusPickedUpResponse -> {
                // Звук бонуса
            }

            is GameStopResponse -> {
                modifyState {
                    copy(
                        isGameRunning = false
                    )
                }

                for ((playerName, playerTime) in message.result) {
                    val thisPlayerResult = PlayerResult(
                        playerName = playerName,
                        finishTime = (playerTime * 1000).toLong(),
                        isCurrentPlayer = playerName == stateValue.mainPlayer.car.playerName
                    )

                    println("$playerName $playerTime")
                    PlayerResultStorage.results = PlayerResultStorage.results.plus(thisPlayerResult)
                }

                gateway.disconnect()
                onFinish()
                soundManager.stopSurfaceSound()
                soundManager.release()
            }

            else -> Unit
        }
    }

    override fun onCleared() {
        super.onCleared()

        gameCycle?.cancel()
        soundManager.stopSurfaceSound()
        soundManager.release()
    }
}
