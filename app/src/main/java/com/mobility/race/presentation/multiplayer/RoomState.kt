package com.mobility.race.presentation.multiplayer


data class RoomState(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean,
    val playerNames: Array<String>,
    val isGameStarted: Boolean,
    val roomId: String
) {
    companion object {
        fun default(playerName: String, roomName: String, isCreatingRoom: Boolean): RoomState {
            return RoomState(
                playerName = playerName,
                roomName = roomName,
                isCreatingRoom = isCreatingRoom,
                playerNames = arrayOf(playerName),
                isGameStarted = false,
                roomId = "",
            )
        }

    }
}