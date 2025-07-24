package com.mobility.race.presentation.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.GameStopResponse
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.InfoResponse
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.LeftRoomResponse
import com.mobility.race.data.PlayerActionResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.PlayerDisconnectedResponse
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.RoomUpdatedResponse
import com.mobility.race.data.ServerMessage
import com.mobility.race.data.StartedGameResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoomViewModel(
    savedStateHandle: SavedStateHandle,
    private val gateway: IGateway
) : ViewModel() {
    private val playerName: String = checkNotNull(savedStateHandle["playerName"])
    private val roomName: String = checkNotNull(savedStateHandle["roomName"])
    private val isCreatingRoom: Boolean = checkNotNull(savedStateHandle["isCreatingRoom"])

    init {
        init()
    }

    private fun init() {
        viewModelScope.launch {
            gateway.connect()
            delay(1000)
            gateway.initPlayer(playerName)
            delay(1000)

            if (isCreatingRoom) {
                gateway.createRoom(roomName)
            } else {
                gateway.joinRoom(roomName)
            }
        }
    }
}