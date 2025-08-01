package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import com.mobility.race.data.StarterPack
import com.mobility.race.domain.Bonus
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class MultiplayerGameState(
    val countdown: Int,
    val mainPlayer: Player,
    val players: List<Player>,
    val gameMap: GameMap,
    val gameCamera: GameCamera,
    val controllingStick: ControllingStick,
    val checkpointManager: CheckpointManager,
    val isGameRunning: Boolean,
    val lapsCompleted: Int,
    val directionAngle: Float?,
    val bonuses: List<Bonus> = emptyList()
) {
    companion object {
        fun default(
            name: String,
            playerNames: Array<String>,
            carSpriteId: String,
            starterPack: StarterPack
        ): MultiplayerGameState {
            var newRouteList: List<Offset> = emptyList()
            var newBonusList: List<Offset> = emptyList()

            starterPack.route.forEach {
                newRouteList = newRouteList.plus(it.transformToOffset())
            }

            val mainPlayer = Player(
                Car(
                    playerName = name,
                    id = carSpriteId,
                    position = starterPack.initialPlayerStates[playerNames.indexOf(name)].transformToOffset(),
                    visualDirection = starterPack.startAngle,
                ),
                isFinished = false
            )

            var players: List<Player> = emptyList()

            for (name: String in playerNames) {
                players = players.plus(
                    element = Player(
                        car = Car(
                            playerName = name,
                            id = getSpriteId(name, playerNames).toString(),
                            position = starterPack.initialPlayerStates[playerNames.indexOf(name)].transformToOffset(),
                            visualDirection = starterPack.startAngle
                        ),
                        isFinished = false
                    )
                )
            }

            println(players)

            val checkpointManager = CheckpointManager(newRouteList)
            checkpointManager.registerCar(mainPlayer.car.id)

            return MultiplayerGameState(
                countdown = 5,
                mainPlayer = mainPlayer,
                players = players,
                gameMap = GameMap(
                    grid = starterPack.mapGrid,
                    width = starterPack.mapWidth,
                    height = starterPack.mapHeight,
                    startCellPos = starterPack.initialPlayerStates.first().transformToOffset(),
                    startAngle = starterPack.startAngle,
                    route = newRouteList,
                    bonusPoints = newBonusList
                ),
                gameCamera = GameCamera(
                    position = mainPlayer.car.position,
                    mapWidth = starterPack.mapWidth,
                    mapHeight = starterPack.mapHeight
                ),
                controllingStick = ControllingStick(),
                checkpointManager = checkpointManager,
                isGameRunning = true,
                lapsCompleted = 0,
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
}

data class Player(
    var car: Car,
    val isFinished: Boolean = false
)
