package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.mobility.race.domain.Car

class Car(
    val playerName: String,
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var numberPlayer: Int = 1
) {
    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 200f
        const val ACCELERATION_RATE = 10f
        const val DECELERATION_RATE = 8f
        const val TURN_RATE = 2.5f
        const val DRIFT_FACTOR = 0.85f
        const val SIZE = 50f
        const val TURN_ANIMATION_SPEED = 0.1f
    }

    // Current state
    private var speed: Float = 0f
    private var directionRad: Float = 0f // Actual direction
    private var visualDirectionRad: Float = 0f // For smooth rendering
    var position: Offset = Offset(0f, 0f)
    private var isAlive: Boolean = true
    private var turnDirection: Float = 0f
    private var isDrifting: Boolean = false

    fun accelerate(deltaTime: Float) {
        if (!isAlive) return
        speed = min(speed + ACCELERATION_RATE * deltaTime, MAX_SPEED)
    }

    fun decelerate(deltaTime: Float) {
        if (!isAlive) return
        speed = max(speed - DECELERATION_RATE * deltaTime, MIN_SPEED)
    }

    fun startTurn(direction: Float) {
        turnDirection = direction
    }

    fun stopTurn() {
        turnDirection = 0f
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        // Handle turning
        if (speed > 0 && turnDirection != 0f) {
            val turnAmount = turnDirection * TURN_RATE * deltaTime * (speed / MAX_SPEED)
            directionRad += turnAmount

            // Smooth visual direction update
            visualDirectionRad += (directionRad - visualDirectionRad) * TURN_ANIMATION_SPEED

            // Check for drift conditions
            isDrifting = speed > MAX_SPEED * 0.5f && abs(turnDirection) > 0.5f
        } else {
            isDrifting = false
            // When not turning, align visual direction with actual direction
            visualDirectionRad += (directionRad - visualDirectionRad) * TURN_ANIMATION_SPEED
        }

        // Update position
        updatePosition(deltaTime)
    }

    private fun updatePosition(deltaTime: Float) {
        if (speed == 0f) return

        val moveDistance = speed * deltaTime
        val effectiveDirection = if (isDrifting) {
            // Apply drift factor to direction
            visualDirectionRad + (directionRad - visualDirectionRad) * DRIFT_FACTOR
        } else {
            visualDirectionRad
        }

        position = Offset(
            x = position.x + moveDistance * cos(effectiveDirection),
            y = position.y + moveDistance * sin(effectiveDirection)
        )
    }

    fun crash(otherCar: Car? = null) {
        if (!isAlive) return

        otherCar?.let {
            if (speed > it.speed) {
                it.crash()
                return
            }
        }

        isAlive = false
        speed = 0f
    }

    // Getters
    fun getSpeed(): Float = speed
    fun getDirection(): Float = directionRad
    fun getVisualDirection(): Float = visualDirectionRad
    fun isAlive(): Boolean = isAlive
    fun isDrifting(): Boolean = isDrifting
}