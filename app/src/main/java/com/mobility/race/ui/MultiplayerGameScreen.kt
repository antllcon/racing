package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.MultiplayerGameViewModel
import kotlin.math.min

@Composable
fun MultiplayerGameScreen(
    playerName: String,
    roomName: String,
    isCreatingRoom: Boolean,
    viewModel: MultiplayerGameViewModel,
    modifier: Modifier = Modifier
) {
    // Состояние игры
    val gameState by viewModel.gameState.collectAsState()
    val localPlayerId = gameState.localPlayerId

    // Флаг инициализации
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = localPlayerId) {

        // Первое создание объектов для передачи их во viewModel
        if (localPlayerId.isNotBlank() && !isInitialized) {
            val playerCar = Car(id = localPlayerId, playerName = playerName)
            val playerGameMap = GameMap.createRaceTrackMap()
            val playerCamera = GameCamera(
                targetCar = playerCar,
                initialViewportSize = Size.Zero,
                mapSize = playerGameMap.size
            )

            // Передаем их во viewModel
            viewModel.init(
                playerCar = playerCar,
                playerGameMap = playerGameMap,
                playerCamera = playerCamera
            )

            // Запуск цикла
            viewModel.runGame()
            isInitialized = true
        }
    }

    // Обработчик жизненного цикла для остановки игры
    LifecycleEventHandler(onStop = { viewModel.stopGame() })

    // Пока нет viewModel мы ничего не рисуем
    if (!isInitialized) {
        // Можно показать какой-нибудь индикатор загрузки
//         LoadingIndicator()
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                viewModel.camera.setViewportSize(
                    Size(
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                )

            }
    ) {
        val (_, zoom) = viewModel.camera.getViewMatrix()
        val baseCellSize = min(size.width, size.height) / viewModel.gameMap.size.toFloat()

        viewModel.gameMap.drawMap(
            camera = viewModel.camera,
            baseCellSize = baseCellSize,
            zoom = zoom, drawScope = this
        )

        gameState.players.forEach { car ->
            car.drawCar(
                camera = viewModel.camera,
                baseCellSize = baseCellSize,
                zoom = zoom,
                drawScope = this,
                isLocalPlayer = car.id == localPlayerId
            )
        }
    }
}