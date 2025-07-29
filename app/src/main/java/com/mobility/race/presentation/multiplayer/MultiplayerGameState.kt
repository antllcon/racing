package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import com.mobility.race.data.StarterPack
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class MultiplayerGameState(
    val countdown: Float,
    val mainPlayer: Player,
    val players: Array<Player>,
    val gameMap: GameMap,
    val gameCamera: GameCamera,
    val controllingStick: ControllingStick,
    val checkpointManager: CheckpointManager,
    val isGameRunning: Boolean,
    val directionAngle: Float?
) {
    companion object {
        fun default(
            name: String,
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
                Car(
                    playerName = name,
                    id = carSpriteId,
                    position = starterPack.initialPlayerStates[playerNames.indexOf(name)].transformToOffset(),
                    visualDirection = startDirection,
                ),
                isFinished = false
            )

            var players: Array<Player> = emptyArray()

            for (name: String in playerNames) {
                players = players.plus(
                    element = Player(
                        car = Car(
                            playerName = name,
                            id = getSpriteId(name, playerNames).toString(),
                            position = starterPack.initialPlayerStates[playerNames.indexOf(name)].transformToOffset(),
                            visualDirection = startDirection
                        ),
                        isFinished = false
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
                    startCellPos = starterPack.initialPlayerStates.first().transformToOffset(),
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
                isGameRunning = true,
                directionAngle = null
            )
        }

        private fun getSpriteId(playerId: String, playerIds: Array<String>): Int {
            var index = 1
            for (id in playerIds) {
                if (playerId == id) {
                    return index
                }
                index++
            }
            return index
        }
    }

    // TODO: убрать комментарий ниже
    // IDEA generated this shit (lmao) for equaling and hashing arrays usnig Player type
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiplayerGameState

        if (countdown != other.countdown) return false
        if (isGameRunning != other.isGameRunning) return false
        if (directionAngle != other.directionAngle) return false
        if (mainPlayer != other.mainPlayer) return false
        if (!players.contentEquals(other.players)) return false
        if (gameMap != other.gameMap) return false
        if (gameCamera != other.gameCamera) return false
        if (controllingStick != other.controllingStick) return false
        if (checkpointManager != other.checkpointManager) return false

        return true
    }

    override fun hashCode(): Int {
        var result = countdown.hashCode()
        result = 31 * result + isGameRunning.hashCode()
        result = 31 * result + (directionAngle?.hashCode() ?: 0)
        result = 31 * result + mainPlayer.hashCode()
        result = 31 * result + players.contentHashCode()
        result = 31 * result + gameMap.hashCode()
        result = 31 * result + gameCamera.hashCode()
        result = 31 * result + controllingStick.hashCode()
        result = 31 * result + checkpointManager.hashCode()
        return result
    }
}

data class Player(
    val car: Car,
    val isFinished: Boolean = false
)