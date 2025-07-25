package com.mobility.race.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.ui.drawUtils.bitmapStorage
import com.mobility.race.ui.drawUtils.drawBackgroundTexture
import com.mobility.race.ui.drawUtils.drawCar
import com.mobility.race.ui.drawUtils.drawControllingStick
import com.mobility.race.ui.drawUtils.drawGameMap
import com.mobility.race.ui.drawUtils.drawMinimap
import com.mobility.race.ui.drawUtils.drawNextCheckpoint

@Composable
fun MultiplayerGameScreen(
    viewModel: MultiplayerGameViewModel
) {
    val state = viewModel.state.value
    val bitmaps = bitmapStorage()

    var isStickActive by remember { mutableStateOf(false) }
    var currentStickInputAngle: Float? by remember { mutableStateOf(null) }
    var currentStickInputDistanceFactor: Float by remember { mutableFloatStateOf(0f) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.countdown > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = (state.countdown.toInt() + 1).toString())
            }
            return@Box
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                        state.controllingStick.setScreenSize(
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                        state.gameCamera.setNewViewportSize(
                            Size(
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                        )
                    }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (state.controllingStick.isInside(offset)) {
                                        isStickActive = true
                                        val angle = state.controllingStick.getTouchAngle(offset)
                                        viewModel.setDirectionAngle(angle)
                                        currentStickInputAngle = angle
                                        currentStickInputDistanceFactor =
                                            state.controllingStick.getDistanceFactor(offset)
                                    } else {
                                        isStickActive = false
                                        currentStickInputAngle = null
                                        currentStickInputDistanceFactor = 0f
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (isStickActive) {
                                        val angle = state.controllingStick.getTouchAngle(change.position)
                                        viewModel.setDirectionAngle(angle)
                                        currentStickInputAngle = angle
                                        currentStickInputDistanceFactor =
                                            state.controllingStick.getDistanceFactor(change.position)
                                    }
                                },
                                onDragEnd = {
                                    if (isStickActive) {
                                        viewModel.setDirectionAngle(null)
                                        isStickActive = false
                                        currentStickInputAngle = null
                                        currentStickInputDistanceFactor = 0f
                                    }
                                },
                                onDragCancel = {
                                    if (isStickActive) {
                                        viewModel.setDirectionAngle(null)
                                        isStickActive = false
                                        currentStickInputAngle = null
                                        currentStickInputDistanceFactor = 0f
                                    }
                                }
                            )
                }
        ) {
            drawBackgroundTexture(
                state.gameMap,
                state.gameCamera,
                bitmaps["terrain_500"]!!
            )

            drawGameMap(
                state.gameMap,
                state.gameCamera,
                state.gameCamera.viewportSize,
                bitmaps
            )

            drawControllingStick(
                state.controllingStick,
                currentStickInputAngle,
                currentStickInputDistanceFactor
            )

            drawMinimap(state.gameMap, state.mainPlayer.car)

            drawNextCheckpoint(
                state.checkpointManager.getNextCheckpoint(state.mainPlayer.car.id),
                state.gameCamera,
                state.gameCamera.getScaledCellSize(state.gameMap.size)
            )

            drawCar(state, bitmaps)
        }
    }
}

//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.input.pointer.PointerEventType
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.onSizeChanged
//import com.mobility.race.presentation.GameState
//import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
//import com.mobility.race.ui.drawUtils.LifecycleEventHandler
//import kotlin.math.min
//
//@Composable
//fun MultiplayerGameScreen(
//    playerName: String,
//    roomName: String,
//    viewModel: MultiplayerGameViewModel,
//    modifier: Modifier = Modifier
//) {
//    val gameState by viewModel.gameState.collectAsState()
//    val localPlayerId = gameState.localPlayerId
//    val isViewModelReady by viewModel.isViewModelReady.collectAsState()
//
//    LifecycleEventHandler(onStop = { viewModel.stopGame() })
//
//    if (!isViewModelReady) {
//        LoadingScreen()
//        return
//    }
//
//    Canvas(
//        modifier = Modifier.createGameCanvasModifier(viewModel)
//    ) {
//        drawGameContent(viewModel, gameState, localPlayerId, size)
//    }
//}
//
//@Composable
//private fun Modifier.createGameCanvasModifier(viewModel: MultiplayerGameViewModel): Modifier {
//    return this
//        .fillMaxSize()
//        .onSizeChanged { size ->
//            viewModel.onCanvasSizeChanged(
//                Size(
//                    size.width.toFloat(),
//                    size.height.toFloat()
//                )
//            )
//        }
//        .pointerInput(Unit) {
//            awaitPointerEventScope {
//                while (true) {
//                    val event = awaitPointerEvent()
//
//                    when (event.type) {
//                        PointerEventType.Move -> {
//                            val pointer = event.changes.firstOrNull()
//                            pointer?.let {
//                                if (viewModel.isCanvasReady.value) { // Только если Canvas готов
//                                    viewModel.movePlayer(it.position)
//                                }
//                                it.consume()
//                            }
//                        }
//
//                        PointerEventType.Release -> {
//                            if (viewModel.isCanvasReady.value) { // Только если Canvas готов
//                                viewModel.movePlayer(Offset.Zero)
//                            }
//                        }
//
//                        PointerEventType.Press -> {
//                            val pointer = event.changes.firstOrNull()
//                            pointer?.let {
//                                if (viewModel.isCanvasReady.value) { // Только если Canvas готов
//                                    viewModel.movePlayer(it.position)
//                                }
//                                it.consume()
//                            }
//                        }
//
//                        else -> {}
//                    }
//                }
//            }
//        }
//}
//
//private fun DrawScope.drawGameContent(
//    viewModel: MultiplayerGameViewModel,
//    gameState: GameState,
//    localPlayerId: String,
//    size: Size
//) {
//    val (_, zoom) = viewModel.camera.getViewMatrix()
//    val baseCellSize = min(size.width, size.height) / viewModel.map.size.toFloat()
//
////    viewModel.map.drawMap(
////        camera = viewModel.camera,
////        baseCellSize = baseCellSize,
////        zoom = zoom,
////        drawScope = this
////    )
////
////    gameState.players.forEach { car ->
////        car.drawCar(
////            camera = viewModel.camera,
////            drawScope = this,
////            isLocalPlayer = car.id == localPlayerId,
////            scaledCellSize = baseCellSize * zoom
////        )
////    }
//}