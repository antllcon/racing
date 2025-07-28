package com.mobility.race.presentation.multiplayer

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

class MultiplayerGameViewModel(
    playerId: String,
    playerNames: Array<String>,
    carSpriteId: String,
    private val gateway: IGateway
) : BaseViewModel<MultiplayerGameState>(
    MultiplayerGameState.default(
        name = playerId,
        playerNames = playerNames,
        carSpriteId = carSpriteId,
        starterPack = gateway.openGatewayStorage()
    )
) {
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
        Log.d(TAG, "Joystick direction angle set to: $newAngle")
    }

    private fun startGame() {
        var lastTime = System.currentTimeMillis()

        gameCycle = viewModelScope.launch {
            while (stateValue.isGameRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = (currentTime - lastTime) / 1000f

                movePlayers(elapsedTime)
                moveCamera()
                sendPlayerInput()

                lastTime = currentTime
                delay(16)
            }
        }
    }

    // TODO: причесать
    private fun movePlayers(elapsedTime: Float) {
        var newPlayersCopy: Array<Player> = emptyArray()
        var updatedMainPlayer: Player? = null

        for (player in stateValue.players) {
            val speedModifier = stateValue.gameMap.getSpeedModifier(player.car.position)
            val newCar = if (player.name != stateValue.mainPlayer.name) {
                Log.v(TAG, "Client: Moving other player ${player.car.id}. Old Pos: ${player.car.position}, VisualDir: ${player.car.visualDirection}")
                player.car.update(elapsedTime, player.car.visualDirection, speedModifier)
            } else {
                val updatedCarForMainPlayer =
                    player.car.update(elapsedTime, stateValue.directionAngle, speedModifier)
                Log.v(TAG, "Client: Moving main player ${player.car.id}. New Pos: ${updatedCarForMainPlayer.position}, Direction: ${stateValue.directionAngle}")
                updatedMainPlayer = player.copy(car = updatedCarForMainPlayer)
                updatedCarForMainPlayer
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

    private fun moveCamera() {
        modifyState {
            copy(
                gameCamera = gameCamera.update(stateValue.mainPlayer.car.position)
            )
        }
    }

    private suspend fun sendPlayerInput() {
        val playerInput = PlayerInputRequest(
            visualDirection = stateValue.directionAngle ?: stateValue.mainPlayer.car.direction,
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
                println("Server Error: ${message.message}")
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
                        newPlayersList.indexOfFirst { it.car.id == playerDto.id }

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
                        if (existingPlayer.name != stateValue.mainPlayer.name) {
                            Log.i(TAG, "Client: Updated other player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}")
                        }

                        if (existingPlayer.name == stateValue.mainPlayer.name) {
                            updatedMainPlayerFromResponse = updatedPlayer
                            // Логируем, если наш игрок тоже обновляется с сервера (для Server Reconciliation)
                            Log.d(TAG, "Client: Updated main player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}")
                        }
                    } else {
                        Log.w(TAG, "Client: Received DTO for unknown player ID: ${playerDto.id}")
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersList.toTypedArray(),
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