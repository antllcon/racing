package com.mobility.race.presentation.multiplayer

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.PlayerDisconnectedResponse
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
    private val context: Context,
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
        modifyState { currentState ->  // Явно указываем параметр состояния
            currentState.copy(
                isGameStarted = true
            )
        }

        viewModelScope.launch {
            gateway.startGame(stateValue.roomName)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            gateway.disconnect()
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
                Toast.makeText(context, message.message, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is PlayerDisconnectedResponse -> {
                var newPlayersList = emptyArray<String>()

                for (player in stateValue.playerNames) {
                    if (player != message.playerId) {
                        newPlayersList = newPlayersList.plus(player)
                    }
                }

                modifyState {
                    copy(
                        playerNames = newPlayersList
                    )
                }
            }
            is RoomCreatedResponse -> {
                modifyState { currentState ->  // Явно указываем параметр состояния
                    currentState.copy(
                        roomId = message.roomId
                    )
                }
            }
            is JoinedRoomResponse -> {
                modifyState { currentState ->  // Явно указываем параметр состояния
                    currentState.copy(
                        roomId = message.roomId
                    )
                }
            }
            is PlayerConnectedResponse -> {
                modifyState { currentState ->  // Явно указываем параметр состояния
                    currentState.copy(
                        playerNames = message.playerNames
                    )
                }
            }
            is StartedGameResponse -> {
                gateway.fillGatewayStorage(message.starterPack)

                var carSpriteId = 1

                for (name in stateValue.playerNames) {
                    if (name == stateValue.playerName) {
                        break
                    }
                    carSpriteId++
                }

                navController.navigate(route = MultiplayerGame(
                    stateValue.playerName,
                    stateValue.playerNames,
                    carSpriteId.toString()
                ))
            }
            else -> Unit
        }
    }
}