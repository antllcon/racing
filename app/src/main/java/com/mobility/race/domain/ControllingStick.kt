package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class ControllingStick(
    private var screenWidth: Float = 0f,
    private var screenHeight: Float = 0f
) {
    companion object {
        const val SIZE_MULTIPLIER = 0.10f
        const val BOTTOM_OFFSET_MULTIPLIER = 0.05f
        const val STROKE_WIDTH = 20f
    }

    fun setScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    fun getCenter(): Offset {
        val stickRadius = getRadius()
        val centerX = screenWidth / 2f
        val centerY =
            screenHeight - stickRadius - BOTTOM_OFFSET_MULTIPLIER * min(screenWidth, screenHeight)
        return Offset(centerX, centerY)
    }

    fun getRadius(): Float {
        return min(screenWidth, screenHeight) * SIZE_MULTIPLIER
    }

    fun getColor(): Color {
        return Color.DarkGray
    }

    fun getStrokeColor(): Color {
        return Color.Black
    }

    fun getStrokeWidth(): Float {
        return STROKE_WIDTH
    }

    fun isInside(touchPosition: Offset): Boolean {
        val center = getCenter()
        val distance = (touchPosition - center).getDistance()
        return distance <= getRadius()
    }

    fun getTouchAngle(touchPosition: Offset): Float {
        val stickCenter = getCenter()
        return atan2(touchPosition.y - stickCenter.y, touchPosition.x - stickCenter.x)
    }

    fun getDistanceFactor(touchPosition: Offset): Float {
        val stickCenter = getCenter()
        val catheterX = touchPosition.x - stickCenter.x
        val catheterY = touchPosition.y - stickCenter.y
        val hypotenuse = sqrt(catheterX * catheterX + catheterY * catheterY)
        return (hypotenuse / getRadius()).coerceAtMost(1f)
    }
}