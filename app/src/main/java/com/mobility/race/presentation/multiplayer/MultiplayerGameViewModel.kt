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
import com.mobility.race.domain.Car

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
    private val tag = "MultiplayerGameViewModel"

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

    private fun movePlayers(elapsedTime: Float) {
        val playersCopy: Array<Player> = stateValue.players.copyOf()
        var mainPlayerCopy: Player? = null

        for (i: Int in stateValue.players.indices) {
            val player: Player = stateValue.players[i]
            val speedModifier: Float = stateValue.gameMap.getSpeedModifier(position = player.car.position)

            val updatedCar: Car = updatePlayerCar(player, elapsedTime, speedModifier) { updatedCar ->
                if (player.name == stateValue.mainPlayer.name) {
                    mainPlayerCopy = player.copy(car = updatedCar)
                }
            }

            playersCopy[i] = player.copy(car = updatedCar)
        }

        modifyState {
            copy(
                players = playersCopy,
                mainPlayer = mainPlayerCopy ?: mainPlayer
            )
        }
    }

    private fun updatePlayerCar(
        player: Player,
        elapsedTime: Float,
        speedModifier: Float,
        onMainPlayerUpdated: (Car) -> Unit = {}
    ): Car {
        return if (player.name == stateValue.mainPlayer.name) {
            player.car.update(
                elapsedTime = elapsedTime,
                directionAngle = stateValue.directionAngle,
                speedModifier = speedModifier
            ).also { updatedCar ->
                Log.v(
                    tag,
                    "Main player ${player.name} pos: ${updatedCar.position}, vis direction: ${updatedCar.direction}"
                )
                onMainPlayerUpdated(updatedCar)
            }
        } else {
            player.car
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
            directionAngle = stateValue.directionAngle ?: stateValue.mainPlayer.car.direction,
            elapsedTime = elapsedTime,
            ringsCrossed = stateValue.checkpointManager.getLapsForCar(stateValue.mainPlayer.name)
        )
        gateway.playerInput(directionAngle = playerInput.directionAngle, elapsedTime = playerInput.elapsedTime, ringsCrossed = playerInput.ringsCrossed)
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
                var newPlayersArray: Array<Player> = stateValue.players
                var updatedMainPlayerFromResponse: Player? = null

                message.players.forEach { playerDto ->
                    val existingPlayerIndex: Int =
                        newPlayersArray.indexOfFirst { it.car.id == playerDto.id }

                    if (existingPlayerIndex != -1) {
                        val existingPlayer = newPlayersArray[existingPlayerIndex]
                        val newCar = existingPlayer.car.copy(
                            position = Offset(playerDto.posX, playerDto.posY),
                            visualDirection = playerDto.visualDirection,
                            speed = playerDto.speed
                        )
                        val updatedPlayer = existingPlayer.copy(
                            car = newCar,
                            isFinished = playerDto.isFinished
                        )

                        newPlayersArray = newPlayersArray.toMutableList().apply {
                            set(existingPlayerIndex, updatedPlayer)
                        }.toTypedArray()

                        // Логируем, как мы обновляем другого игрока с сервера
                        if (existingPlayer.name != stateValue.mainPlayer.name) {
                            Log.i(
                                tag,
                                "Client: Updated other player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}"
                            )
                        }

                        if (existingPlayer.name == stateValue.mainPlayer.name) {
                            updatedMainPlayerFromResponse = updatedPlayer
                            // Логируем, если наш игрок тоже обновляется с сервера (для Server Reconciliation)
                            Log.d(
                                tag,
                                "Client: Updated main player ${playerDto.id} from server. Pos: (${playerDto.posX}, ${playerDto.posY}), Speed: ${playerDto.speed}, VisualDir: ${playerDto.visualDirection}"
                            )
                        }
                    } else {
                        Log.w(tag, "Client: Received DTO for unknown player ID: ${playerDto.id}")
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersArray,
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