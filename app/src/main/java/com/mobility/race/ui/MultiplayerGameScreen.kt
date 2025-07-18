package com.mobility.race.ui

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.mobility.race.domain.ControllingStick
import com.mobility.race.domain.drawCar
import com.mobility.race.presentation.GameState
import com.mobility.race.presentation.MultiplayerGameViewModel
import com.mobility.race.ui.drawUtils.drawControllingStick
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
    val controllingStick = remember { ControllingStick(Resources.getSystem().displayMetrics.widthPixels) }

    LifecycleEventHandler(onStop = { viewModel.stopGame() })

    if (!isViewModelReady) {
        LoadingScreen()
        return
    }

    Canvas(
        modifier = Modifier.createGameCanvasModifier(viewModel, controllingStick)
    ) {
        drawControllingStick(controllingStick)
        drawGameContent(viewModel, gameState, localPlayerId, size)
    }
}

@Composable
private fun Modifier.createGameCanvasModifier(viewModel: MultiplayerGameViewModel, controllingStick: ControllingStick): Modifier {
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
        detectDragGestures (
            onDrag = { change, _ ->
                if (controllingStick.isTouchInsideStick(change.position)) {
                    viewModel.setDirectionAngle(controllingStick.getTouchAngle(change.position))
                } else {
                    viewModel.setDirectionAngle(null)
                }
            },
            onDragEnd = {
                viewModel.setDirectionAngle(null)
            }
        )
    }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    if (controllingStick.isTouchInsideStick(offset)) {
                        viewModel.setDirectionAngle(controllingStick.getTouchAngle(offset))
                    }
                    if (tryAwaitRelease()) {
                        viewModel.setDirectionAngle(null)
                    }
                }
            )
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
            isLocalPlayer = car.id == localPlayerId,
            scaledCellSize = baseCellSize * zoom
        )
    }
}