package mobility.domain

import androidx.compose.ui.geometry.Offset
import com.mobility.race.domain.Car
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
    val newSpeed = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
    val newDirection = if (newSpeed > 0.001f) atan2(velocity.y, velocity.x) else this.direction

    return this.copy(
        position = position,
        speed = newSpeed.coerceIn(Car.MIN_SPEED, Car.MAX_SPEED),
        direction = newDirection,
        visualDirection = newDirection
    )
}