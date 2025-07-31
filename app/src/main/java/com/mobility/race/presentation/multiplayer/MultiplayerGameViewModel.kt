package com.mobility.race.presentation.multiplayer

import SoundManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.GameStopResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.PlayerInputRequest
import com.mobility.race.data.PlayerResultStorage
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.Car
import com.mobility.race.domain.CollisionManager
import com.mobility.race.presentation.BaseViewModel
import com.mobility.race.ui.MultiplayerGame
import com.mobility.race.ui.MultiplayerRaceFinished
import com.mobility.race.ui.PlayerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val targetPlayerPositions: MutableMap<String, Offset> = mutableMapOf()
    private val targetPlayerDirections: MutableMap<String, Float> = mutableMapOf()

    private val INTERPOLATION_FACTOR = 0.2f
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
        // Логируем ввод с джойстика
//        Log.d(TAG, "Joystick direction angle set to: $newAngle")
    }

    private fun startGame() {
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            while (stateValue.isGameRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = (currentTime - lastTime) / 1000f

//                CollisionManager.checkAndResolveCollisions(stateValue.mainPlayer, stateValue.players)
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
        var playersCopy: List<Player> = emptyList()
        var mainPlayerCopy: Player? = null

        for (player: Player in stateValue.players) {
            val speedModifier: Float = stateValue.gameMap.getSpeedModifier(position = player.car.position)
            var updatedCar: Car

            if (player.isFinished) {
                updatedCar = player.car
            } else {
                if (player.car.playerName != stateValue.mainPlayer.car.playerName) {
                    val targetPosition = targetPlayerPositions[player.car.playerName]
                    val targetDirection = targetPlayerDirections[player.car.playerName]

                    if (targetPosition != null && targetDirection != null) {
                        val interpolatedX = player.car.position.x + (targetPosition.x - player.car.position.x) * INTERPOLATION_FACTOR
                        val interpolatedY = player.car.position.y + (targetPosition.y - player.car.position.y) * INTERPOLATION_FACTOR

                        val interpolatedDirection = player.car.direction + (targetDirection - player.car.direction) * INTERPOLATION_FACTOR

                        updatedCar = player.car.update(
                            elapsedTime = elapsedTime,
                            directionAngle = interpolatedDirection,
                            speedModifier = speedModifier
                        ).setNewPosition(Offset(interpolatedX, interpolatedY))
                    } else {
                        updatedCar = player.car.update(elapsedTime, directionAngle = player.car.direction, speedModifier)
                    }
                } else {
                    val updatedCarForMainPlayer: Car = player.car.update(elapsedTime, directionAngle = stateValue.directionAngle, speedModifier)
                    mainPlayerCopy = player.copy(car = updatedCarForMainPlayer)
                    updatedCar = updatedCarForMainPlayer
                }
            }

            playersCopy = playersCopy.plus(element = player.copy(car = updatedCar))
        }

        modifyState {
            copy(
                players = playersCopy,
                mainPlayer = mainPlayerCopy ?: mainPlayer
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

            if (stateValue.lapsCompleted >= 1) {
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
        if (stateValue.players[currentActivePlayerId].isFinished) {
            for (i in 0 until stateValue.players.size) {
                if (!stateValue.players[i].isFinished) {
                    currentActivePlayerId = i
                    break
                }

                println(i)
            }
        }

        modifyState {
            copy(
                gameCamera = gameCamera.update(stateValue.players[currentActivePlayerId].car.position)
            )
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
            is GameCountdownUpdateResponse -> {
                modifyState {
                    copy(
                        countdown = message.remainingTime
                    )
                }
            }

            is GameStateUpdateResponse -> {
                var newPlayersList: List<Player> = stateValue.players.toList()
                var updatedMainPlayerFromResponse: Player? = null

                message.players.forEach { playerDto ->
                    val existingPlayerIndex: Int =
                        newPlayersList.indexOfFirst { it.car.playerName == playerDto.id }

                    if (existingPlayerIndex != -1) {
                        val existingPlayer = newPlayersList[existingPlayerIndex]

                        if (existingPlayer.car.playerName != stateValue.mainPlayer.car.playerName) {

                            targetPlayerPositions[playerDto.id] = Offset(playerDto.posX, playerDto.posY)
                            targetPlayerDirections[playerDto.id] = playerDto.visualDirection

                            val newCar = existingPlayer.car.copy(
                                visualDirection = playerDto.visualDirection,
                                speed = playerDto.speed
                            )
                            val updatedPlayer = existingPlayer.copy(
                                car = newCar,
                                isFinished = playerDto.isFinished
                            )
                            newPlayersList = newPlayersList.toMutableList().apply {
                                set(existingPlayerIndex, updatedPlayer)
                            }.toList()

                        } else {
                            val serverPos = Offset(playerDto.posX, playerDto.posY)
                            val currentClientPos = stateValue.mainPlayer.car.position

                            val distanceDiff = (serverPos - currentClientPos).getDistance()

                            if (distanceDiff > POSITION_TOLERANCE) {
                                val correctionVector = (serverPos - currentClientPos) / distanceDiff
                                val correctedX = currentClientPos.x + correctionVector.x * CORRECTION_SPEED * elapsedTime
                                val correctedY = currentClientPos.y + correctionVector.y * CORRECTION_SPEED * elapsedTime

                                val newCar = existingPlayer.car.copy(
                                    position = Offset(correctedX, correctedY),
                                    visualDirection = playerDto.visualDirection,
                                    speed = playerDto.speed
                                )
                                val updatedPlayer = existingPlayer.copy(
                                    car = newCar,
                                    isFinished = playerDto.isFinished
                                )
                                newPlayersList = newPlayersList.toMutableList().apply {
                                    set(existingPlayerIndex, updatedPlayer)
                                }.toList()
                                updatedMainPlayerFromResponse = updatedPlayer
                            } else {
                                val newCar = existingPlayer.car.copy(
                                    visualDirection = playerDto.visualDirection,
                                    speed = playerDto.speed
                                )
                                val updatedPlayer = existingPlayer.copy(
                                    car = newCar,
                                    isFinished = playerDto.isFinished
                                )
                                newPlayersList = newPlayersList.toMutableList().apply {
                                    set(existingPlayerIndex, updatedPlayer)
                                }.toList()
                                updatedMainPlayerFromResponse = updatedPlayer
                            }
                        }
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersList,
                        mainPlayer = updatedMainPlayerFromResponse ?: mainPlayer
                    )
                }
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