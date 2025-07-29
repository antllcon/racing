package com.mobility.race.presentation.multiplayer


data class RoomState(
    val playerId: String,
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean,
    val playersName: List<String>,
    val playersId: List<String>,
    val isGameStarted: Boolean,
    val roomId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomState

        if (playerId != other.playerId) return false
        if (playerName != other.playerName) return false
        if (roomName != other.roomName) return false
        if (isCreatingRoom != other.isCreatingRoom) return false
        if (playersName != other.playersName) return false
        if (playersId != other.playersId) return false
        if (isGameStarted != other.isGameStarted) return false
        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerId.hashCode()
        result = 31 * result + playerName.hashCode()
        result = 31 * result + roomName.hashCode()
        result = 31 * result + isCreatingRoom.hashCode()
        result = 31 * result + playersName.hashCode()
        result = 31 * result + playersId.hashCode()
        result = 31 * result + isGameStarted.hashCode()
        result = 31 * result + roomId.hashCode()
        return result
    }

    companion object {
        fun default(playerName: String, roomName: String, isCreatingRoom: Boolean): RoomState {
            return RoomState(
                playerId = "",
                playerName = playerName,
                roomName = roomName,
                isCreatingRoom = isCreatingRoom,
                playersName = listOf(playerName),
                playersId = emptyList(),
                isGameStarted = false,
                roomId = "",
            )
        }
    }
}
