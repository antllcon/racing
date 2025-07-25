package com.mobility.race.presentation.multiplayer


data class RoomState(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean,
    val playerNames: Map<String, String>,
    val isGameStarted: Boolean,
    val roomId: String,
    val playerId: String
) {
    companion object {
        fun default(playerName: String, roomName: String, isCreatingRoom: Boolean): RoomState {
            return RoomState(
                playerName = playerName,
                roomName = roomName,
                isCreatingRoom = isCreatingRoom,
                playerNames = mapOf(playerName to ""),
                isGameStarted = false,
                roomId = "",
                playerId = ""
            )
        }

    }
}