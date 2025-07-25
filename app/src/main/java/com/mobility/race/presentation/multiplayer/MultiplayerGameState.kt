package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class MultiplayerGameState(
    val countdown: Float,
    val mainPlayer: Player,
    val players: Array<Player>,
    val gameMap: GameMap?,
    val gameCamera: GameCamera?,
    val controllingStick: ControllingStick,
    val checkpointManager: CheckpointManager?,
    val directionAngle: Float?
) {
    companion object {
        fun default(playerId: String, nickname: String, carSpriteId: String): MultiplayerGameState {
            return MultiplayerGameState(
                countdown = 5f,
                mainPlayer = Player(playerId, Car(playerName = nickname, id = carSpriteId)),
                players = emptyArray(),
                gameMap = null,
                gameCamera = null,
                controllingStick = ControllingStick(),
                checkpointManager = null,
                directionAngle = null
            )
        }
    }
}

data class Player(
    val id: String,
    val car: Car,
    val isAccelerating: Boolean = false,
    val isFinished: Boolean = false
)