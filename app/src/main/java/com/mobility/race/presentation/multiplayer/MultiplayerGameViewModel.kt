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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MultiplayerGameViewModel(
    playerId: String,
    playerNames: Array<String>,
    carSpriteId: String,
    gateway: IGateway
): BaseViewModel<MultiplayerGameState>(MultiplayerGameState.default(
    playerId = playerId,
    playerIds = playerNames,
    carSpriteId = carSpriteId,
    starterPack = gateway.openGatewayStorage()
)) {

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)
    }

    fun setDirectionAngle(newAngle: Float?) {
        modifyState {
            copy(
                directionAngle = newAngle
            )
        }
    }

    private fun updateGame(players: List<PlayerStateDto>) {

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

            }
            else -> Unit
        }
    }
}