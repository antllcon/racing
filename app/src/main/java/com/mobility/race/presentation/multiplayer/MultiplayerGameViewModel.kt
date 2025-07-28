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
                player.car.update(elapsedTime, player.car.visualDirection, speedModifier)
            } else {
                val updatedCarForMainPlayer =
                    player.car.update(elapsedTime, stateValue.directionAngle, speedModifier)
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
                throw Exception("Think about it!")
            }

            is GameCountdownUpdateResponse -> {
                modifyState {
                    copy(
                        countdown = message.remainingTime
                    )
                }
            }

            is GameStateUpdateResponse -> {
                var updatedPlayers: Array<Player> = emptyArray()
                var updatedMainPlayerFromResponse: Player? = null

                for (player in message.players) {
                    val existingPlayer =
                        stateValue.players.find { it.car.playerName == player.name }
                    existingPlayer?.let {
                        val newCar = it.car.copy(
                            position = Offset(player.posX, player.posY),
                            visualDirection = player.visualDirection,
                            speed = player.speed
                        )
                        val updatedPlayer = Player(
                            player.name,
                            newCar,
                            player.isFinished
                        )
                        updatedPlayers = updatedPlayers.plus(updatedPlayer)

                        if (player.name == stateValue.mainPlayer.name) {
                            updatedMainPlayerFromResponse = updatedPlayer
                        }
                    }
                }

                modifyState {
                    copy(
                        players = updatedPlayers,
                        mainPlayer = updatedMainPlayerFromResponse ?: mainPlayer
                    )
                }
            }

            else -> Unit
        }
    }
}