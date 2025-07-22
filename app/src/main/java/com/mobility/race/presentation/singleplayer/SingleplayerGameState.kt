package com.mobility.race.presentation.singleplayer
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
        fun default(carId: String): SingleplayerGameState {
            val gameMap: GameMap = GameMap.generateDungeonMap()
            val car = Car(position = gameMap.finishCellPos, id = carId)

            return SingleplayerGameState(
                directionAngle = null,
                isGameRunning = false,
                controllingStick = ControllingStick(),
                car = car,
                gameMap = gameMap,
                gameCamera = GameCamera(
                    position = car.position,
                    viewportSize = Size.Unspecified,
                    mapWidth = gameMap.width,
                    mapHeight = gameMap.height
                )
            )
        }
    }
}
