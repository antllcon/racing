package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


data class CollisionResult(
    val areColliding: Boolean,
    val penetrationVector: Offset
)

fun Car.getVelocity(): Offset {
    return Offset(cos(visualDirection) * speed, sin(visualDirection) * speed)
}

fun Car.withNewPhysicsState(position: Offset, velocity: Offset): Car {
    val newSpeed = velocity.magnitude()
    val newDirection = if (newSpeed > 0.001f) atan2(velocity.y, velocity.x) else this.direction

    return this.copy(
        position = position,
        speed = newSpeed.coerceIn(Car.MIN_SPEED, Car.MAX_SPEED),
        direction = newDirection,
        visualDirection = newDirection
    )
}

data class Projection(
    val min: Float,
    val max: Float
)

fun Offset.magnitude(): Float {
    return sqrt(x * x + y * y)
}

fun Offset.normalized(): Offset {
    val mag = magnitude()
    return if (mag != 0f) this / mag else Offset.Zero
}

fun Offset.dot(other: Offset): Float {
    return x * other.x + y * other.y
}