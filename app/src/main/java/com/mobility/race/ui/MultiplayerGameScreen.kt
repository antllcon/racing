package com.mobility.race.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.MultiplayerGameViewModel

@Composable
fun MultiplayerGameScreen(
    playerName: String,
    roomName: String,
    isCreatingRoom: Boolean,
    viewModel: MultiplayerGameViewModel,
    modifier: Modifier = Modifier
) {
    val players: List<Car> by viewModel.players.collectAsState()
    val gameStatus: String by viewModel.gameStatus.collectAsState()
    val errorMessage: String? by viewModel.errorMessage.collectAsState()

    LifecycleEventHandler(
        onCreate = {
            val playerCar = Car(id = "придумать как получать id", playerName = playerName)
            val playerGameMap = GameMap.createRaceTrackMap()

            viewModel.init(
                playerCar = playerCar,
                playerGameMap = playerGameMap,
                playerCamera = GameCamera(
                    targetCar = playerCar,
                    initialViewportSize = Size.Zero,
                    mapSize = playerGameMap.size
                )
            )
        },

        // другие колбеки

        onStop = { viewModel.stopGame() }
    )


    Column(modifier = modifier) {
        Text("Multiplayer room name: $roomName")
        Text("Game status: $gameStatus")
        Text("User status: ${if (isCreatingRoom) "admin" else "player"}")
        errorMessage?.let {
            Text("Error: $it")
        }

        Text("Players: ${players.joinToString { it.playerName }}")
    }
}