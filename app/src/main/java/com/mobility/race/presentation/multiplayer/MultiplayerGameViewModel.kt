package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.PlayerStateDto
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
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
    gateway: IGateway
): BaseViewModel<MultiplayerGameState>(MultiplayerGameState.default(
    nickname = playerId,
    playerNames = playerNames,
    carSpriteId = carSpriteId,
    starterPack = gateway.openGatewayStorage()
)) {
    var gameCycle: Job? = null

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
                val elapsedTime = (currentTime - lastTime) / 1000f

                movePlayers(elapsedTime)
//                checkCheckpoints()
//                moveCamera()
                lastTime = currentTime

                delay(16)
            }
        }
    }

    private fun movePlayers(elapsedTime: Float) {
        var newPlayersCopy: Array<Player> = emptyArray()

        for (player in stateValue.players) {
            val speedModifier = stateValue.gameMap.getSpeedModifier(player.car.position)
            val newCar = player.car.update(elapsedTime, player.car.visualDirection, speedModifier)

            newPlayersCopy = newPlayersCopy.plus(player.copy(
                car = newCar
            ))
        }

        modifyState {
            copy(
                players = newPlayersCopy
            )
        }
    }


    private fun handleMessage(message: ServerMessage) {
        when (message) {
            is GameCountdownUpdateResponse -> {
                modifyState {
                    copy(
                        countdown = message.remainingTime
                    )
                }
            }
            is GameStateUpdateResponse -> {
                var updatedPlayers: Array<Player> = emptyArray()

                for (player in message.players) {
                    val playerCar = stateValue.players.find { it.car.playerName == player.name }

                    updatedPlayers = updatedPlayers.plus(
                        Player(
                            player.name,
                            playerCar!!.car.copy(
                                position = Offset(player.posX, player.posY),
                                visualDirection = player.visualDirection,
                                speed = player.speed
                            ),
                            player.isAccelerating,
                            player.isFinished
                        )
                    )
                }

                modifyState {
                    copy(
                        players = updatedPlayers
                    )
                }
            }
            else -> Unit
        }
    }
}