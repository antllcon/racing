package com.mobility.race.ui.drawUtils

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.mobility.race.domain.ControllingStick


fun DrawScope.drawControllingStick(controllingStick: ControllingStick)  {
    drawCircle(
        center = controllingStick.getCenter(),
        radius = controllingStick.getRadius(),
        color = controllingStick.getColor(),
        alpha = 0.85f
    )
}