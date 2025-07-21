package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobility.race.domain.ControllingStick
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawControllingStick(
    controllingStick: ControllingStick,
    currentInputAngle: Float?,
    currentInputDistanceFactor: Float
) {
    val strokeColor = Color.Black

    // Внешний круг
    drawCircle(
        center = controllingStick.getCenter(),
        radius = controllingStick.getRadius(),
        color = controllingStick.getColor()
    )

    // Внутренний круг
    drawCircle(
        center = controllingStick.getCenter(),
        radius = controllingStick.getRadius(),
        color = strokeColor,
        style = Stroke(controllingStick.getStrokeWidth())
    )

    if (currentInputAngle != null) {
        val stickCenter = controllingStick.getCenter()
        val stickRadius = controllingStick.getRadius()

        val innerCircleMaxTravelDistance = stickRadius * 0.6f
        val innerCircleTravelDistance = currentInputDistanceFactor * innerCircleMaxTravelDistance

        val innerCircleCenter = Offset(
            x = stickCenter.x + innerCircleTravelDistance * cos(currentInputAngle),
            y = stickCenter.y + innerCircleTravelDistance * sin(currentInputAngle)
        )

        val innerCircleRadius = stickRadius * 0.3f
        val innerCircleColor = strokeColor

        drawCircle(
            center = innerCircleCenter,
            radius = innerCircleRadius,
            color = innerCircleColor
        )
    }
}
