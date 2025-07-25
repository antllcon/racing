package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.ServerMessage
import com.mobility.race.data.StartedGameResponse
import com.mobility.race.presentation.BaseViewModel
import com.mobility.race.ui.MultiplayerGame
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RoomViewModel(
    private val playerName: String,
    private val roomName: String,
    private val isCreatingRoom: Boolean,
    private val navController: NavController,
    private val gateway: IGateway
) : BaseViewModel<RoomState>(RoomState.default(playerName, roomName, isCreatingRoom)) {

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)

        init()
    }

    fun startGame() {
        modifyState {
            copy(
                isGameStarted = true
            )
        }

        viewModelScope.launch {
            gateway.startGame(stateValue.roomName)
        }

    }

    private fun init() {
        viewModelScope.launch {
            gateway.connect()
            gateway.initPlayer(playerName)

            if (isCreatingRoom) {
                gateway.createRoom(roomName)
            } else {
                gateway.joinRoom(roomName)
            }
        }
    }


    private fun handleMessage(message: ServerMessage) {
        when (message) {
            is ErrorResponse -> {
                throw Exception("Think about it!")
            }
            is RoomCreatedResponse -> {
                modifyState {
                    copy(
                        roomId = message.roomId
                    )
                }
            }
            is JoinedRoomResponse -> {
                modifyState {
                    copy(
                        roomId = message.roomId
                    )
                }
            }
            is PlayerConnectedResponse -> {
                val players =

                modifyState {
                    copy(
                        players = players.plus(Playerz)
                    )
                }
            }
            is StartedGameResponse -> {
                gateway.fillGatewayStorage(message.starterPack)

                var carSpriteId = ""

                for (i in 0 until stateValue.players.size) {
                    if (stateValue.players[i].car.playerName == stateValue.playerName) {
                        carSpriteId = i.toString()
                    }
                }

                navController.navigate(route = MultiplayerGame(stateValue.playerId, stateValue.roomName, carSpriteId))
            }
            else -> Unit
        }
    }
}
