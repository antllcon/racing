package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MultiplayerGameViewModel(
    playerId: String,
    playerName: String,
    carSpriteId: String,
    gateway: IGateway
): BaseViewModel<MultiplayerGameState>(MultiplayerGameState.default(
    playerId = playerId,
    nickname = playerName,
    carSpriteId = carSpriteId
)) {

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)

        val unpackedStorage = gateway.openGatewayStorage()
        var newRouteList: List<Offset> = emptyList()

        unpackedStorage.route.forEach {
            newRouteList = newRouteList.plus(it.transformToOffset())
        }

        modifyState {
            copy(
                gameMap = GameMap(
                    unpackedStorage.mapGrid,
                    unpackedStorage.mapWidth,
                    unpackedStorage.mapHeight,
                    unpackedStorage.startPosition.transformToOffset(),
                    unpackedStorage.startDirection,
                    newRouteList
                ),
                gameCamera = GameCamera(
                    position = mainPlayer.car.position,
                    mapWidth = unpackedStorage.mapWidth,
                    mapHeight = unpackedStorage.mapHeight
                ),
                checkpointManager = CheckpointManager(newRouteList)
            )
        }
    }

    fun setDirectionAngle(newAngle: Float?) {
        modifyState {
            copy(
                directionAngle = newAngle
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

            }
            else -> Unit
        }
    }
}