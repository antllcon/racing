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
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.ui.drawUtils.LifecycleEventHandler
import kotlin.math.min

@Composable
fun MultiplayerGameScreen(
    playerName: String,
    roomName: String,
    viewModel: MultiplayerGameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val localPlayerId = gameState.localPlayerId
    val isViewModelReady by viewModel.isViewModelReady.collectAsState()

    LifecycleEventHandler(onStop = { viewModel.stopGame() })

    if (!isViewModelReady) {
        LoadingScreen()
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
            viewModel.onCanvasSizeChanged(
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
                        PointerEventType.Move -> {
                            val pointer = event.changes.firstOrNull()
                            pointer?.let {
                                if (viewModel.isCanvasReady.value) { // Только если Canvas готов
                                    viewModel.movePlayer(it.position)
                                }
                                it.consume()
                            }
                        }

                        PointerEventType.Release -> {
                            if (viewModel.isCanvasReady.value) { // Только если Canvas готов
                                viewModel.movePlayer(Offset.Zero)
                            }
                        }

                        PointerEventType.Press -> {
                            val pointer = event.changes.firstOrNull()
                            pointer?.let {
                                if (viewModel.isCanvasReady.value) { // Только если Canvas готов
                                    viewModel.movePlayer(it.position)
                                }
                                it.consume()
                            }
                        }

                        else -> {}
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

//    viewModel.map.drawMap(
//        camera = viewModel.camera,
//        baseCellSize = baseCellSize,
//        zoom = zoom,
//        drawScope = this
//    )
//
//    gameState.players.forEach { car ->
//        car.drawCar(
//            camera = viewModel.camera,
//            drawScope = this,
//            isLocalPlayer = car.id == localPlayerId,
//            scaledCellSize = baseCellSize * zoom
//        )
//    }
}