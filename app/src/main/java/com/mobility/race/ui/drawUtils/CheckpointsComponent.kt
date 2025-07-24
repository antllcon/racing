package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobility.race.domain.GameCamera

fun DrawScope.drawNextCheckpoint(
    nextCheckpoint: Offset?,
    gameCamera: GameCamera,
    scaledCellSize: Float
) {
    if (nextCheckpoint != null) {
        val checkpointWorldPos = Offset(nextCheckpoint.x + 0.5f, nextCheckpoint.y + 0.5f)
        val checkpointScreenPos = gameCamera.worldToScreen(checkpointWorldPos)
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.5f),
            radius = scaledCellSize * 0.6f,
            center = checkpointScreenPos,
            style = Stroke(width = scaledCellSize * 0.1f)
        )
    }
}