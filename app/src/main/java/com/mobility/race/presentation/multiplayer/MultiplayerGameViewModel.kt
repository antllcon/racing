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
import com.mobility.race.domain.Car

class MultiplayerGameViewModel(
    playerId: String,
    playerName: String,
    playersName: List<String>,
    playersId: List<String>,
    private val gateway: IGateway
) : BaseViewModel<MultiplayerGameState>(
    MultiplayerGameState.default(
        playerId = playerId,
        playerName = playerName,
        playersName = playersName,
        playersId = playersId,
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

    private fun movePlayers(elapsedTime: Float) {
        val playersCopy: Array<Player> = stateValue.players.copyOf()
        var mainPlayerCopy: Player? = null

        for (i: Int in stateValue.players.indices) {
            val player: Player = stateValue.players[i]
            val speedModifier: Float = stateValue.gameMap.getSpeedModifier(position = player.car.position)

            val updatedCar: Car = updatePlayerCar(player, elapsedTime, speedModifier) { updatedCar ->
                if (player.car.id == stateValue.mainPlayer.car.id) {
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
        return if (player.car.id == stateValue.mainPlayer.car.id) {
            player.car.update(
                elapsedTime = elapsedTime,
                directionAngle = stateValue.directionAngle,
                speedModifier = speedModifier
            ).also { updatedCar ->
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
            ringsCrossed = stateValue.checkpointManager.getLapsForCar(stateValue.mainPlayer.car.id)
        )
        gateway.playerInput(directionAngle = playerInput.directionAngle, elapsedTime = playerInput.elapsedTime, ringsCrossed = playerInput.ringsCrossed)
    }

    private fun handleMessage(message: ServerMessage) {
        when (message) {
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
                var newPlayersArray: Array<Player> = stateValue.players.copyOf()

                for (player: Player in newPlayersArray)
                {
                    println(player.car.id)
                }


                var newMainPlayer: Player? = null

                message.players.forEach { playerDto ->
                    val existPlayerIndex: Int = newPlayersArray.indexOfFirst {
                        it.car.id == playerDto.id
                    }

                    println("DTO ID = ${playerDto.id}, CAR ID = ${newPlayersArray[0].car.id}")

                    if (existPlayerIndex != -1) {
                        val existPlayer: Player = newPlayersArray[existPlayerIndex]
                        val updateCar: Car = existPlayer.car.copy(
                            position = Offset(
                                x = playerDto.posX,
                                y = playerDto.posY
                            ),
                            visualDirection = playerDto.visualDirection,
                            speed = playerDto.speed
                        )
                        val updatePlayer: Player = existPlayer.copy(
                            car = updateCar,
                            isFinished = playerDto.isFinished
                        )

                        newPlayersArray = newPlayersArray.apply {
                            set(existPlayerIndex, updatePlayer)
                        }

                        if (existPlayer.car.id == stateValue.mainPlayer.car.id) {
                            newMainPlayer = updatePlayer
                        }
                    } else {
                        println("Unknown player ID: ${playerDto.id}")
                    }
                }

                modifyState {
                    copy(
                        players = newPlayersArray,
                        mainPlayer = newMainPlayer ?: mainPlayer
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