package com.mobility.race.presentation.multiplayer

import com.mobility.race.domain.Car


data class RoomState(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean,
    val isGameStarted: Boolean,
    val roomId: String,
    val playerId: String,
    val players: Array<Player>
) {
    companion object {
        fun default(playerName: String, roomName: String, isCreatingRoom: Boolean): RoomState {
            return RoomState(
                playerName = playerName,
                roomName = roomName,
                isCreatingRoom = isCreatingRoom,
                isGameStarted = false,
                roomId = "",
                playerId = "",
                players = arrayOf(),
            )
        }

    }
}