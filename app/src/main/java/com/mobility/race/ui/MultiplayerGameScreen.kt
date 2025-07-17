package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.presentation.GameState
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
    val gameState by viewModel.gameState.collectAsState()
    val localPlayerId = gameState.localPlayerId
    val isViewModelReady by viewModel.isViewModelReady.collectAsState()

    LifecycleEventHandler(onStop = { viewModel.stopGame() })

    if (!isViewModelReady) {
        // Можно показать какой-нибудь индикатор загрузки, текст "Загрузка..."
        return
    }

    Canvas(
        modifier = Modifier.createGameCanvasModifier(viewModel)
    ) {
        drawGameContent(viewModel, gameState, localPlayerId, size)
    }
}

@Composable
private fun Modifier.createGameCanvasModifier(viewModel: MultiplayerGameViewModel): Modifier {
    return this
        .fillMaxSize()
        .onSizeChanged { size ->
            viewModel.camera.setViewportSize(
                Size(
                    size.width.toFloat(),
                    size.height.toFloat()
                )
            )
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    when (event.type) {
                        // Если палец движется (или просто нажат и не отпущен)
                        PointerEventType.Move -> {
                            // Получаем позицию первого пальца (или любого активного указателя)
                            val pointer = event.changes.firstOrNull()
                            pointer?.let {
                                viewModel.movePlayer(it.position) // Отправляем текущую позицию
                                it.consume() // Потребляем событие, чтобы оно не распространялось дальше
                            }
                        }
                        // Если палец отпущен
                        PointerEventType.Release -> {
                            viewModel.movePlayer(Offset.Zero) // Отправляем Offset.Zero, чтобы остановить машину
                        }
                        // Дополнительно можно обработать PointerEventType.Press, если нужно реагировать на первое нажатие
                        PointerEventType.Press -> {
                            val pointer = event.changes.firstOrNull()
                            pointer?.let {
                                viewModel.movePlayer(it.position) // Начинаем движение сразу при нажатии
                                it.consume()
                            }
                        }
                        else -> {
                            // Игнорируем другие типы событий
                        }
                    }
                }
            }
        }
}

private fun DrawScope.drawGameContent(
    viewModel: MultiplayerGameViewModel,
    gameState: GameState,
    localPlayerId: String,
    size: Size
) {
    val (_, zoom) = viewModel.camera.getViewMatrix()
    val baseCellSize = min(size.width, size.height) / viewModel.map.size.toFloat()

    viewModel.map.drawMap(
        camera = viewModel.camera,
        baseCellSize = baseCellSize,
        zoom = zoom,
        drawScope = this
    )

    gameState.players.forEach { car ->
        car.drawCar(
            camera = viewModel.camera,
            drawScope = this,
            isLocalPlayer = car.id == localPlayerId
        )
    }
}