package com.mobility.race.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                ON_CREATE -> {
                    val playerCar = Car(id = "localPlayerId", playerName = playerName)
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
                }

//                ON_START -> viewModel.runGame() // Сигнал для запуска игровой логики
                ON_STOP -> viewModel.stopGame() // Сигнал для остановки игровой логики
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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