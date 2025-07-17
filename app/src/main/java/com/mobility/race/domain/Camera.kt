package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

class Camera(
    private val targetCar: Car,
    initialViewportSize: Size,
    private val mapSize: Int = 10
) {
    companion object {
        private const val BASE_SMOOTHNESS = 0.1f
        private const val FIXED_ZOOM = 28f
        private const val FPS_NORMALIZATION = 60f
        private const val LOOK_AHEAD_FACTOR = 0.25f
    }

    private var _currentPosition: Offset = targetCar.position
    private var _viewportSize: Size = initialViewportSize

    val viewportSize: Size
        get() = _viewportSize

    val currentPosition: Offset
        get() = _currentPosition

    val zoom: Float
        get() = FIXED_ZOOM

    fun update(deltaTime: Float) {
        _currentPosition = lerp(
            _currentPosition,
            calculateIdealPosition(),
            BASE_SMOOTHNESS * deltaTime * FPS_NORMALIZATION
        )
    }

    fun getViewMatrix(): Pair<Offset, Float> = _currentPosition to FIXED_ZOOM

    fun worldToScreen(worldPos: Offset): Offset {
        val cellSize = calculateCellSize()
        return (worldPos - _currentPosition) * cellSize * FIXED_ZOOM +
                Offset(_viewportSize.width / 2, _viewportSize.height / 2)
    }

    fun screenToWorld(screenPos: Offset): Offset {
        val cellSize = calculateCellSize()
        return (screenPos - Offset(_viewportSize.width / 2, _viewportSize.height / 2)) /
                (cellSize * FIXED_ZOOM) + _currentPosition
    }

    fun setViewportSize(newSize: Size) {
        _viewportSize = newSize
    }

    private fun calculateIdealPosition(): Offset {
        val lookAhead = Offset(
            cos(targetCar.direction) * LOOK_AHEAD_FACTOR,
            sin(targetCar.direction) * LOOK_AHEAD_FACTOR
        ) * targetCar.speed.coerceIn(0f, 1f)

        return targetCar.position + lookAhead
    }

    private fun calculateCellSize(): Float {
        return min(_viewportSize.width, _viewportSize.height) / mapSize
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount.coerceIn(0f, 1f)
    }

    private fun lerp(start: Offset, end: Offset, amount: Float): Offset {
        return Offset(
            lerp(start.x, end.x, amount),
            lerp(start.y, end.y, amount)
        )
    }
}