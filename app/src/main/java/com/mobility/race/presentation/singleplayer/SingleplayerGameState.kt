package com.mobility.race.presentation.singleplayer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class SingleplayerGameState(
    val directionAngle: Float?,
    val isGameRunning: Boolean,
    val controllingStick: ControllingStick,
    val car: Car,
    val gameMap: GameMap,
    val gameCamera: GameCamera,
    val checkpointManager: CheckpointManager,
    val lapsCompleted: Int = 0,
    val totalLaps: Int = 3
) {
    companion object {
        fun default(carId: String): SingleplayerGameState {
            val gameMap: GameMap = GameMap.generateDungeonMap()
            val checkpointManager = CheckpointManager(gameMap.route)
            val initialDirection = when (gameMap.startDirection) {
                GameMap.StartDirection.HORIZONTAL -> Car.DIRECTION_RIGHT
                GameMap.StartDirection.VERTICAL -> Car.DIRECTION_UP
            }
            val car = Car(
                position = Offset(gameMap.startCellPos.x + 0.4f, gameMap.startCellPos.y + 0.5f),
                id = carId,
                direction = initialDirection,
                visualDirection = initialDirection
            )

            checkpointManager.registerCar(car.id)

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
                ),
                checkpointManager = checkpointManager
            )
        }
    }
}
