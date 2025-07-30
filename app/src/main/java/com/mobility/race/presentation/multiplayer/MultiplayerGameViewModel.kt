package com.mobility.race.presentation.multiplayer

import SoundManager
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.PlayerInputRequest
import com.mobility.race.data.ServerMessage
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.util.Log
import androidx.navigation.NavController
import com.mobility.race.data.GameStopResponse
import com.mobility.race.domain.Car
import com.mobility.race.ui.MenuScreen
import com.mobility.race.ui.MultiplayerMenuScreen
import com.mobility.race.ui.SingleplayerGame

class MultiplayerGameViewModel(
    playerId: String,
    playerNames: Array<String>,
    carSpriteId: String,
    private val navController: NavController,
    private val gateway: IGateway
) : BaseViewModel<MultiplayerGameState>(
    MultiplayerGameState.default(
        name = playerId,
        playerNames = playerNames,
        carSpriteId = carSpriteId,
        starterPack = gateway.openGatewayStorage()
    )
) {
    private var currentActivePlayerId = stateValue.players.indexOfFirst { it == stateValue.mainPlayer }
    private var gameCycle: Job? = null
    private var elapsedTime: Float = 0f
    private val TAG = "MultiplayerGameViewModel"

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)

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

                movePlayers(elapsedTime)
                moveCamera()
                checkCheckpoints()

                if (!stateValue.mainPlayer.isFinished) {
                    sendPlayerInput()
                }

                lastTime = currentTime
                delay(16)
            }
        }
    }

    // TODO: причесать
    private fun movePlayers(elapsedTime: Float) {
        var newPlayersCopy: List<Player> = emptyList()
        var updatedMainPlayer: Player? = null

        for (player in stateValue.players) {
            val speedModifier = stateValue.gameMap.getSpeedModifier(player.car.position)
            var newCar: Car

            if (!player.isFinished) {
                if (player.car.playerName != stateValue.mainPlayer.car.playerName) {
                    newCar = player.car.update(elapsedTime, player.car.visualDirection, speedModifier)
                } else {
                    val updatedCarForMainPlayer =
                        player.car.update(elapsedTime, stateValue.directionAngle, speedModifier)
                    updatedMainPlayer = player.copy(car = updatedCarForMainPlayer)
                    newCar = updatedCarForMainPlayer
                }
            } else {
                newCar = player.car
            }

            newPlayersCopy = newPlayersCopy.plus(player.copy(car = newCar))
        }


        modifyState {
            copy(
                players = newPlayersCopy,
                mainPlayer = updatedMainPlayer ?: mainPlayer
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
                val newMainPlayer = stateValue.mainPlayer.copy(
                    isFinished = true
                )
                var newPlayers = emptyList<Player>()

                for (player in stateValue.players) {
                    newPlayers = if (player != stateValue.mainPlayer) {
                        newPlayers.plus(player)
                    } else {
                        newPlayers.plus(newPlayers)
                    }
                }

                modifyState {
                    copy(
                        mainPlayer = newMainPlayer,
                        players = newPlayers
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

    private fun handleMessage(message: ServerMessage) {
        when (message) {
            // TODO: проверить можно ли подключаться во время запущенной игры
            // TODO: перекидывать в наблюдателей (если есть место в комнате)
            is ErrorResponse -> {
//                println("Server Error: ${message.message}")
            }

            is GameStopResponse -> {
                modifyState {
                    copy(
                        isGameRunning = false
                    )
                }

                Log.d("FINISH", message.result.toString())
                gameCycle?.cancel()
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
                        val newCar = existingPlayer.car.copy(
                            position = Offset(playerDto.posX, playerDto.posY),
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

                        // Логируем, как мы обновляем другого игрока с сервера
                        if (existingPlayer.car.playerName != stateValue.mainPlayer.car.playerName) {
//                            Log.i(TAG, "Client: Updated other player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}")
                        }

                        if (existingPlayer.car.playerName == stateValue.mainPlayer.car.playerName) {
                            updatedMainPlayerFromResponse = updatedPlayer
                            // Логируем, если наш игрок тоже обновляется с сервера (для Server Reconciliation)
//                            Log.d(TAG, "Client: Updated main player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}")
                        }
                    } else {
//                        Log.w(TAG, "Client: Received DTO for unknown player ID: ${playerDto.id}")
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersList,
                        mainPlayer = updatedMainPlayerFromResponse ?: mainPlayer
                    )
                }

                // TODO: Здесь должна быть более сложная логика Server Reconciliation для mainPlayer.
                // Вместо простого копирования, можно интерполировать или корректировать позицию.
                // Например:
                // if (updatedMainPlayerFromResponse != null) {
                //     val serverPos = updatedMainPlayerFromResponse.car.position
                //     val clientPos = stateValue.mainPlayer.car.position
                //     // Сравнить serverPos и clientPos
                //     // Если расхождение велико, плавно сгладить или телепортнуть
                //     // Можно сохранить serverPos и currentClientPos и интерполировать между ними
                // }
            }

            else -> Unit
        }
    }
}