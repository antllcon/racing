package com.mobility.race.data

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class Vector2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    fun transformToOffset(): Offset {
        return Offset(x, y)
    }
}