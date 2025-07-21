package com.mobility.race.presentation.singleplayer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mobility.race.domain.Car
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class SingleplayerGameState(
    val directionAngle: Float?,
    val isGameRunning: Boolean,
    val controllingStick: ControllingStick,
    val car: Car,
    val gameMap: GameMap,
    val gameCamera: GameCamera
) {
    companion object {
        fun default(): SingleplayerGameState {
            val car = Car(position = Offset(5f, 5f))

            return SingleplayerGameState(
                directionAngle = null,
                isGameRunning = false,
                controllingStick = ControllingStick(),
                car = car,
                gameMap = GameMap.createRaceTrackMap(),
                gameCamera = GameCamera(position = car.position, viewportSize = Size.Unspecified)
            )
        }
    }
}
