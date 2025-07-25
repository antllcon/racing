package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.min

data class GameCamera(
    val position: Offset,
    var viewportSize: Size = Size.Unspecified,
    val mapWidth: Int,
    val mapHeight: Int
) {
    companion object {
        private const val BASE_SMOOTHNESS = 0.1f
        private const val FIXED_ZOOM = 4f
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

    fun getScaledCellSize(mapSize: Int): Float {
        val (_, zoom) = getViewMatrix()
        val baseCellSize = min(viewportSize.width, viewportSize.height) / mapSize.toFloat()
        val scaledCellSize = baseCellSize * zoom

        return scaledCellSize
    }

    fun getViewMatrix(): Pair<Offset, Float> = position to FIXED_ZOOM

    fun worldToScreen(worldPos: Offset): Offset {
        val cellSize: Float = calculateCellSize()
        return (worldPos - position) * cellSize * FIXED_ZOOM + Offset(
            x = viewportSize.width / 2,
            y = viewportSize.height / 2
        )
    }

    fun screenToWorld(screenPos: Offset): Offset {
        val cellSize = calculateCellSize()
        return (screenPos - Offset(viewportSize.width / 2, viewportSize.height / 2)) /
                (cellSize * FIXED_ZOOM) + position
    }

    fun setNewViewportSize(newSize: Size) {
        viewportSize = newSize
    }

    private fun calculateCellSize(): Float {
        return min(a = viewportSize.width, b = viewportSize.height) / mapWidth
    }
}