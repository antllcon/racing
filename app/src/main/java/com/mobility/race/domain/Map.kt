package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.max

class Map private constructor(
    private val _terrainGrid: Array<Array<TerrainType>>
) {
    enum class TerrainType(val speedModifier: Float) {
        ABYSS(0.0f),
        GRASS(0.2f),
        ROAD(1.0f)
    }

    companion object {
        private const val MAP_SIZE = 10
        private const val CENTER_POSITION = 4.5
        private const val INNER_TRACK = 1.5
        private const val MIDDLE_ZONE = 3.0
        private const val OUTER_TRACK = 4.5

        fun createRaceTrackMap(): Map {
            val mapData = Array(MAP_SIZE) { IntArray(MAP_SIZE) { 1 } }

            for (i in 0 until MAP_SIZE) {
                for (j in 0 until MAP_SIZE) {
                    val distance = max(abs(i - CENTER_POSITION), abs(j - CENTER_POSITION))
                    mapData[i][j] = when {
                        distance < INNER_TRACK -> 2
                        distance < MIDDLE_ZONE -> 1
                        distance < OUTER_TRACK -> 2
                        else -> 1
                    }
                }
            }

            return Map(validateAndConvert(mapData))
        }

        private fun validateAndConvert(mapData: Array<IntArray>): Array<Array<TerrainType>> {
            require(mapData.size == MAP_SIZE && mapData.all { it.size == MAP_SIZE }) {
                "Map data must be a ${MAP_SIZE}x$MAP_SIZE grid"
            }

            return Array(MAP_SIZE) { i ->
                Array(MAP_SIZE) { j ->
                    when (mapData[i][j]) {
                        0 -> TerrainType.ABYSS
                        1 -> TerrainType.GRASS
                        2 -> TerrainType.ROAD
                        else -> throw IllegalArgumentException("Invalid terrain type at ($i, $j)")
                    }
                }
            }
        }
    }

    val size: Int get() = _terrainGrid.size

    fun getTerrainAt(x: Int, y: Int): TerrainType {
        require(x in _terrainGrid.indices && y in _terrainGrid[0].indices) {
            "Coordinates ($x, $y) out of bounds"
        }
        return _terrainGrid[x][y]
    }

    fun isMovable(x: Int, y: Int): Boolean {
        return getTerrainAt(x, y) != TerrainType.ABYSS
    }

    fun getSpeedModifier(x: Int, y: Int): Float {
        return getTerrainAt(x, y).speedModifier
    }

    fun drawMap(
        camera: Camera,
        baseCellSize: Float,
        zoom: Float,
        drawScope: DrawScope
    ) {
        val scaledCellSize = baseCellSize * zoom

        for (i in 0 until size) {
            for (j in 0 until size) {
                val worldPos = Offset(j.toFloat(), i.toFloat())
                val screenPos = camera.worldToScreen(worldPos)

                val color = when (getTerrainAt(i, j)) {
                    TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                    TerrainType.GRASS -> Color(0xFF4CAF50)
                    TerrainType.ROAD -> Color(0xFF616161)
                }

                drawScope.drawRect(
                    color = color,
                    topLeft = screenPos,
                    size = Size(scaledCellSize, scaledCellSize)
                )

                drawScope.drawRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    topLeft = screenPos,
                    size = Size(scaledCellSize, scaledCellSize),
                    style = Stroke(1f)
                )
            }
        }
    }
}