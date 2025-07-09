package com.mobility.race.domain

class GameMap(private val mapData: Array<IntArray>) {
    enum class TerrainType(val speedModifier: Float) {
        ABYSS(0.0f),    // Пропасть/водоем
        GRASS(0.4f),     // Трава
        ROAD(1.0f)       // Дорога (асфальт)
    }

    private val terrainGrid: Array<Array<TerrainType>> = Array(10) { Array(10) { TerrainType.GRASS } }

    init {
        validateMapData()
        initializeTerrain()
    }

    private fun validateMapData() {
        require(mapData.size == 10 && mapData.all { it.size == 10 }) {
            "Map data must be a 10x10 grid"
        }
    }

    private fun initializeTerrain() {
        for (i in 0 until 10) {
            for (j in 0 until 10) {
                terrainGrid[i][j] = when (mapData[i][j]) {
                    0 -> TerrainType.ABYSS
                    1 -> TerrainType.GRASS
                    2 -> TerrainType.ROAD
                    else -> throw IllegalArgumentException("Invalid terrain type at ($i, $j)")
                }
            }
        }
    }
    //позже заменить отрисовку на генерацию
    companion object {
        fun createRaceTrackMap(): GameMap {
            val mapData = Array(10) { IntArray(10) { 1 } } // По умолчанию трава

            for (i in 0 until 10) {
                for (j in 0 until 10) {
                    val distanceToCenter = maxOf(kotlin.math.abs(i - 4.5), kotlin.math.abs(j - 4.5))

                    when {
                        distanceToCenter < 1.5 -> mapData[i][j] = 2  // дорога
                        distanceToCenter < 3.0 -> mapData[i][j] = 1  // трава
                        distanceToCenter < 4.5 -> mapData[i][j] = 2  // дорога
                        else -> mapData[i][j] = 1                   // трава
                    }
                }
            }

            return GameMap(mapData)
        }
    }

    fun getTerrainAt(x: Int, y: Int): TerrainType {
        require(x in 0..9 && y in 0..9) { "Coordinates must be between 0 and 9" }
        return terrainGrid[x][y]
    }

    fun isMovable(x: Int, y: Int): Boolean {
        return getTerrainAt(x, y) != TerrainType.ABYSS
    }

    fun getSpeedModifier(x: Int, y: Int): Float {
        return getTerrainAt(x, y).speedModifier
    }
}