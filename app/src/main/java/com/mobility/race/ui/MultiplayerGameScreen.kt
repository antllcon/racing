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
    roomName: String,
    playerName: String,
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
                    val playerCar = Car(playerName = playerName)
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

                ON_START -> viewModel.runGame()
                ON_STOP -> viewModel.stopGame()
                // ON_RESUME, ON_PAUSE, ON_DESTROY, ON_ANY - пока не использую
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier) {
        Text("Multiplayer Game in Room: $roomName")
        Text("Game Status: $gameStatus")
        errorMessage?.let {
            Text("Error: $it")
        }

        Text("Players: ${players.joinToString { it.playerName }}")

        // TODO: Добавить в будущем здесь UI для игры и обработку касаний
    }
}