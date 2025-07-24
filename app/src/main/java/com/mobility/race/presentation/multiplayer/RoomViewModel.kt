package com.mobility.race.presentation.multiplayer

import androidx.lifecycle.viewModelScope
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.ServerMessage
import com.mobility.race.data.StartedGameResponse
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RoomViewModel(
    private val playerName: String,
    private val roomName: String,
    private val isCreatingRoom: Boolean,
    private val navigateToMultiplayerGame: () -> Unit,
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
                modifyState {
                    copy(
                        playerNames = message.playerNames
                    )
                }
            }
            is StartedGameResponse -> {
                navigateToMultiplayerGame()
            }
            else -> Unit
        }
    }
}
