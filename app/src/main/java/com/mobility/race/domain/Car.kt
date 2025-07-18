package com.mobility.race.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlin.math.*

data class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var id: String = "1",
    var position: Offset = Offset.Unspecified,
    var corners: List<Offset> = emptyList(),
    var speed: Float = 0f,
    var direction: Float = 0f,
    var visualDirection: Float = 0f,
    var speedModifier: Float = 1f,
) {

    fun update(elapsedTime: Float, directionAngle: Float?, gameMap: GameMap): Car {
        return copy(
            direction = handleAnglesDiff(directionAngle),
            position = updatePosition(elapsedTime),
            speed = updateSpeed(directionAngle),
            speedModifier = setSpeedModifier(gameMap),
            visualDirection = updateVisualDirection()
        )
    }

    private fun updateSpeed(directionAngle: Float?): Float {
        if (directionAngle == null) {
            decelerate()
        } else {
            accelerate()
        }

        return speed
    }

    private fun handleAnglesDiff(newAngle: Float?): Float {
        if (newAngle != null) {
            direction = newAngle
        }
        return direction
    }

    private fun setSpeedModifier(gameMap: GameMap): Float {
        val cellX = position.x.toInt().coerceIn(0, gameMap.size - 1)
        val cellY = position.y.toInt().coerceIn(0, gameMap.size - 1)

        return gameMap.getSpeedModifier(cellX, cellY).coerceIn(0f, 1f)
    }

    private fun updatePosition(deltaTime: Float): Offset {
        val moveDistance = speed * deltaTime * speedModifier
        val maxMove = MAP_SIZE * 0.5f
        val actualMove = moveDistance.coerceIn(-maxMove, maxMove)

        val newPosition = Offset(
            x = (position.x + actualMove * cos(visualDirection)),
            y = (position.y + actualMove * sin(visualDirection))
        )

         return newPosition
    }

    private fun decelerate() {
        if (speed > MIN_SPEED) {
            speed -= DECELERATION
        }
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED
        }
    }

    private fun accelerate() {
        if (speed < MAX_SPEED) {
            speed += ACCELERATION
        }
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED
        }
    }

    private fun updateVisualDirection(): Float {
        var directionShift =
            (direction - visualDirection) * VISUAL_LAG_SPEED * (1 + speed / MAX_SPEED)

        if (abs(directionShift) > MAX_DIRECTION_CHANGE) {
            if (directionShift > 0) {
                directionShift = MAX_DIRECTION_CHANGE
            } else {
                directionShift = -MAX_DIRECTION_CHANGE
            }
        }

        visualDirection += directionShift

        return visualDirection
    }
//    fun update(elapsedTime: Float) {
//        val safeDeltaTime = elapsedTime.coerceIn(0.001f, 0.1f)
//        updateTurnInput(safeDeltaTime)
//        updateDriftState()
//        updateTurning(safeDeltaTime)
//        updatePosition(safeDeltaTime)
//        updateVisualDirection(safeDeltaTime)
//        updateCorners()
//    }
//
//    private fun updateCorners() {
//        val halfLength = LENGTH / 2
//        val halfWidth = WIDTH / 2
//
//        val frontLeft = Offset(-halfLength, -halfWidth)
//        val frontRight = Offset(-halfLength, halfWidth)
//        val rearLeft = Offset(halfLength, -halfWidth)
//        val rearRight = Offset(halfLength, halfWidth)
//
//        val rotatedCorners = listOf(frontLeft, frontRight, rearLeft, rearRight).map { corner ->
//            rotatePoint(corner, _direction)
//        }
//
//        corners = rotatedCorners.map { corner ->
//            Offset(position.value.x + corner.x, position.value.y + corner.y)
//        }
//    }
//
//    private fun rotatePoint(point: Offset, angle: Float): Offset {
//        val cosA = cos(angle)
//        val sinA = sin(angle)
//        return Offset(
//            x = point.x * cosA - point.y * sinA,
//            y = point.x * sinA + point.y * cosA
//        )
//    }
//
//    fun checkCollision(other: Car): Boolean {
//        if (this.corners.isEmpty() || other.corners.isEmpty()) return false
//
//        if (!getBoundingBox().overlaps(other.getBoundingBox())) {
//            return false
//        }
//
//        return satCollisionCheck(this, other)
//    }
//
//    private fun getBoundingBox(): Rect {
//        if (corners.isEmpty()) return Rect(position.value, Size.Zero)
//
//        val minX = corners.minOf { it.x }
//        val minY = corners.minOf { it.y }
//        val maxX = corners.maxOf { it.x }
//        val maxY = corners.maxOf { it.y }
//
//        return Rect(minX, minY, maxX, maxY)
//    }
//
//    private fun satCollisionCheck(car1: Car, car2: Car): Boolean {
//        val axes = mutableListOf<Offset>()
//
//        for (i in 0 until car1.corners.size) {
//            val p1 = car1.corners[i]
//            val p2 = car1.corners[(i + 1) % car1.corners.size]
//            val edge = p1 - p2
//            axes.add(Offset(edge.y, -edge.x).normalized())
//        }
//
//        for (i in 0 until car2.corners.size) {
//            val p1 = car2.corners[i]
//            val p2 = car2.corners[(i + 1) % car2.corners.size]
//            val edge = p1 - p2
//            axes.add(Offset(edge.y, -edge.x).normalized())
//        }
//
//        for (axis in axes) {
//            if (!overlapOnAxis(car1, car2, axis)) {
//                return false
//            }
//        }
//
//        return true
//    }
//
//    private fun overlapOnAxis(car1: Car, car2: Car, axis: Offset): Boolean {
//        val proj1 = projectCorners(car1.corners, axis)
//        val proj2 = projectCorners(car2.corners, axis)
//
//        return proj1.first <= proj2.second && proj2.first <= proj1.second
//    }
//
//    private fun projectCorners(corners: List<Offset>, axis: Offset): Pair<Float, Float> {
//        var min = Float.POSITIVE_INFINITY
//        var max = Float.NEGATIVE_INFINITY
//
//        for (corner in corners) {
//            val dot = corner.x * axis.x + corner.y * axis.y
//            min = minOf(min, dot)
//            max = maxOf(max, dot)
//        }
//
//        return Pair(min, max)
//    }
//
//    private fun Offset.normalized(): Offset {
//        val length = sqrt(x * x + y * y)
//        return if (length > 0) Offset(x / length, y / length) else this
//    }
//
//    private fun updateTurnInput(deltaTime: Float) {
//        _turnInput = lerp(_turnInput, _targetTurnInput, 0.2f, deltaTime)
//    }
//
//
//    private fun updateTurning(deltaTime: Float) {
//        if (_speed == 0f || _turnInput == 0f) return
//
//        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
//        val turnAmount = _turnInput * turnRate * deltaTime * sqrt(_speed / MAX_SPEED)
//        _direction += turnAmount
//    }
//
//
//    private fun updateVisualDirection(deltaTime: Float) {
//        val targetDirection = if (_isDrifting) {
//            _direction + (DRIFT_ANGLE_OFFSET * _turnInput)
//        } else {
//            _direction
//        }
//
//        val lagFactor = VISUAL_LAG_SPEED * deltaTime * 60f
//        _visualDirection = lerp(_visualDirection, targetDirection, lagFactor.coerceIn(0f, 0.5f), deltaTime)
//    }
//
//    fun accelerate(deltaTime: Float) {
//        _targetSpeed = min(MAX_SPEED * _speedModifier, _targetSpeed + ACCELERATION * deltaTime * _speedModifier)
//        _speed = lerp(_speed, _targetSpeed, 0.1f, deltaTime)
//    }
//
//    fun decelerate(deltaTime: Float) {
//        _targetSpeed = max(MIN_SPEED, _targetSpeed - DECELERATION * deltaTime * _speedModifier)
//        _speed = lerp(_speed, _targetSpeed, 0.1f, deltaTime)
//    }
//
//    private fun lerp(start: Float, end: Float, factor: Float, deltaTime: Float): Float {
//        return start + (end - start) * factor * deltaTime * 60f
//    }
//
//    fun startTurn(direction: Float) {
//        _targetTurnInput = direction.coerceIn(-1f, 1f)
//    }
//
//    fun stopTurn() {
//        _targetTurnInput = 0f
//    }
//
//    fun reset(position: Offset = Offset(5f, 5f)) {
//        this.position.value = position
//        _speed = 0f
//        _direction = 0f
//        _visualDirection = 0f
//        _turnInput = 0f
//        _isDrifting = false
//        _speedModifier = 1f
//        updateCorners()
//    }

    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 0.05f
        const val ACCELERATION = 0.002f
        const val DECELERATION = 0.001f
        const val BASE_TURN_RATE = 1.2f
        const val DRIFT_TURN_RATE = 2.5f
        const val DRIFT_SPEED_THRESHOLD = 0.03f
        const val WIDTH = 0.035f
        const val LENGTH = 0.06f
        const val MAP_SIZE = 10f
        const val MAX_DIRECTION_CHANGE = 0.01f

        const val VISUAL_LAG_SPEED = 0.05f
        const val DRIFT_ANGLE_OFFSET = 0.2f
    }
}