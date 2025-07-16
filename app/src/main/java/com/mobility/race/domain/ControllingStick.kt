package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.sqrt

class ControllingStick(private val minScreenSize: Int) {
    fun isTouchInsideStick(dragPosition: Offset): Boolean {
        val catheterX = dragPosition.x - getCenter().x
        val catheterY = dragPosition.y - getCenter().y
        val hypotenuse = sqrt(catheterX * catheterX + catheterY * catheterY)

        return getRadius() >= hypotenuse
    }

    fun getCenter(): Offset {
        return Offset(minScreenSize * (STICK_SIZE_MULTIPLIER + STICK_OFFSET_MULTIPLIER), minScreenSize * (STICK_SIZE_MULTIPLIER + STICK_OFFSET_MULTIPLIER + STICK_LEFT_BORDER_OFFSET_MULTIPLIER))
    }

    fun getRadius(): Float {
        return minScreenSize * STICK_SIZE_MULTIPLIER
    }

    fun getColor(): Color {
        return Color.Gray
    }

    fun getTouchAngle(touchPosition: Offset): Float {
        val angle = atan2(
            x = touchPosition.x,
            y = touchPosition.y
        )

        return angle
    }

    companion object {
        const val STICK_SIZE_MULTIPLIER = 0.15f
        const val STICK_OFFSET_MULTIPLIER = 0.02f
        const val STICK_LEFT_BORDER_OFFSET_MULTIPLIER = 0.08f
    }
}