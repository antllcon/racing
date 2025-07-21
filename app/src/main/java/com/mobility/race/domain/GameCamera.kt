package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

data class GameCamera(
    val position: Offset,
    var viewportSize: Size = Size.Unspecified,
    val mapSize: Int = 10,
) {
    companion object {
        private const val BASE_SMOOTHNESS = 0.1f
        private const val FIXED_ZOOM = 12f
        private const val FPS_NORMALIZATION = 60f
        private const val LOOK_AHEAD_FACTOR = 0.25f
    }

    val zoom: Float
        get() = FIXED_ZOOM

    fun update(newPosition: Offset): GameCamera {
        return copy(
            position = newPosition
        )
    }

    fun getViewMatrix(): Pair<Offset, Float> = position to FIXED_ZOOM

    fun worldToScreen(worldPos: Offset): Offset {
        val cellSize = calculateCellSize()
        return (worldPos - position) * cellSize * FIXED_ZOOM +
                Offset(viewportSize.width / 2, viewportSize.height / 2)
    }

    fun setNewViewportSize(newSize: Size) {
        viewportSize = newSize
    }

    private fun calculateCellSize(): Float {
        return min(viewportSize.width, viewportSize.height) / mapSize
    }
}