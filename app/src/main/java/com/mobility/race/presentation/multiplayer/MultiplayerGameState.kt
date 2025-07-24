package com.mobility.race.presentation.multiplayer

data class MultiplayerGameState(
    val countdown: Float
) {
    companion object {
        fun default(): MultiplayerGameState {
            return MultiplayerGameState(
                countdown = 5f
            )
        }
    }
}