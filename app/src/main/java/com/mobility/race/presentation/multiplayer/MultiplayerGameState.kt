package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import com.mobility.race.data.StarterPack
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

data class MultiplayerGameState(
    val countdown: Float,
    val mainPlayer: Player,
    val players: Array<Player>,
    val gameMap: GameMap,
    val gameCamera: GameCamera,
    val controllingStick: ControllingStick,
    val checkpointManager: CheckpointManager,
    val directionAngle: Float?
) {
    companion object {
        fun default(
            nickname: String,
            playerNames: Array<String>,
            carSpriteId: String,
            starterPack: StarterPack
        ): MultiplayerGameState {
            var newRouteList: List<Offset> = emptyList()

            starterPack.route.forEach {
                newRouteList = newRouteList.plus(it.transformToOffset())
            }

            val startDirection =
                if (starterPack.startDirection == GameMap.StartDirection.VERTICAL) {
                    Car.DIRECTION_UP
                } else {
                    Car.DIRECTION_RIGHT
                }


            val mainPlayer = Player(
                nickname, Car(
                    id = carSpriteId,
                    position = starterPack.startPosition.transformToOffset(),
                    visualDirection = startDirection
                )
            )

            var players: Array<Player> = emptyArray()

            for (name in playerNames) {
                players = players.plus(
                    Player(
                        name = name,
                        Car(
                            id = getSpriteId(name, playerNames).toString(),
                            visualDirection = startDirection
                        )
                    )
                )
            }

            return MultiplayerGameState(
                countdown = 5f,
                mainPlayer = mainPlayer,
                players = players,
                gameMap = GameMap(
                    grid = starterPack.mapGrid,
                    width = starterPack.mapWidth,
                    height = starterPack.mapHeight,
                    startCellPos = starterPack.startPosition.transformToOffset(),
                    startDirection = starterPack.startDirection,
                    route = newRouteList
                ),
                gameCamera = GameCamera(
                    position = mainPlayer.car.position,
                    mapWidth = starterPack.mapWidth,
                    mapHeight = starterPack.mapHeight
                ),
                controllingStick = ControllingStick(),
                checkpointManager = CheckpointManager(newRouteList),
                directionAngle = null
            )
        }


        private fun getSpriteId(playerIdToFind: String, playerIds: Array<String>): Int {
            var index = 1
            for (playerId in playerIds) {
                if (playerId == playerIdToFind) {
                    return index
                }
                index++
            }
            return index
        }
    }
}

data class Player(
    val name: String,
    val car: Car,
    val isAccelerating: Boolean = false,
    val isFinished: Boolean = false
)