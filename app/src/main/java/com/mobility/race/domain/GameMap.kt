package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.random.Random

class GameMap private constructor(
    private val grid: Array<IntArray>,
    val width: Int,
    val height: Int,
    val finishCellPos: Offset
) {
    enum class TerrainType(val speedModifier: Float) {
        ROAD(speedModifier = 1.0f),
        GRASS(speedModifier = 0.2f),
        ABYSS(speedModifier = .0f)
    }

    companion object {
        private const val DEFAULT_MAP_WIDTH = 12
        private const val DEFAULT_MAP_HEIGHT = 12
        private const val DEFAULT_CORE_POINT = 6

        fun generateDungeonMap(
            width: Int = DEFAULT_MAP_WIDTH,
            height: Int = DEFAULT_MAP_HEIGHT,
            roomCount: Int = DEFAULT_CORE_POINT
        ): GameMap {
            val grid: Array<IntArray> = Array(size = height) { IntArray(size = width) }

            generateCoresInternal(grid, roomCount)
            generateRoadsInternal(grid)
            determinationCellTypesInternal(grid)
            val finishCellPos: Offset = findStartCellInternal(grid)

            return GameMap(grid, width, height, finishCellPos)
        }

        private fun generateCoresInternal(grid: Array<IntArray>, roomCount: Int) {
            val width: Int = grid[0].size
            val height: Int = grid.size

            var generatedRooms = 0
            while (generatedRooms < roomCount) {
                val x: Int = (Random.nextInt(until = (width - 2) / 2) * 2) + 1
                val y: Int = (Random.nextInt(until = (height - 2) / 2) * 2) + 1

                if (grid[y][x] == 0) {
                    grid[y][x] = 1
                    generatedRooms++
                }
            }
        }

        private fun generateRoadsInternal(grid: Array<IntArray>) {
            val rooms: MutableList<Offset> = mutableListOf()

            for (y in 1 until grid.size - 1) {
                for (x in 1 until grid[y].size - 1) {
                    if (grid[y][x] == 1) rooms.add(Offset(x.toFloat(), y.toFloat()))
                }
            }

            if (rooms.size < 2) return

            val centerX: Float = grid[0].size / 2f
            val centerY: Float = grid.size / 2f

            rooms.sortWith(compareBy {
                atan2((it.y - centerY).toDouble(), (it.x - centerX).toDouble()).toFloat()
            })

            rooms.add(rooms.first())

            for (i in 1 until rooms.size) {
                var x1 = rooms[i - 1].x.toInt()
                var y1 = rooms[i - 1].y.toInt()
                val x2 = rooms[i].x.toInt()
                val y2 = rooms[i].y.toInt()

                while (x1 != x2) {
                    x1 += if (x2 > x1) 1 else -1
                    if (grid[y1][x1] == 0) grid[y1][x1] = 2
                }
                while (y1 != y2) {
                    y1 += if (y2 > y1) 1 else -1
                    if (grid[y1][x1] == 0) grid[y1][x1] = 2
                }
            }
        }

        private fun determinationCellTypesInternal(grid: Array<IntArray>) {
            val width = grid[0].size
            val height = grid.size

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    var index = 0
                    val currentCellType = grid[y][x]

                    val top = grid[y - 1][x]
                    val bottom = grid[y + 1][x]
                    val left = grid[y][x - 1]
                    val right = grid[y][x + 1]

                    if (currentCellType == 1) {
                        if (top == 0 && bottom == 0 && left != 0 && right != 0) index = 101
                        else if (top != 0 && bottom != 0 && left == 0 && right == 0) index = 102
                        else if (top != 0 && bottom == 0 && left == 0 && right != 0) index = 103
                        else if (top != 0 && bottom == 0 && left != 0 && right == 0) index = 104
                        else if (top == 0 && bottom != 0 && left == 0 && right != 0) index = 105
                        else if (top == 0 && bottom != 0 && left != 0 && right == 0) index = 106

                    } else if (currentCellType == 2) {
                        if (top == 0 && bottom == 0 && left != 0 && right != 0) index = 201
                        else if (top != 0 && bottom != 0 && left == 0 && right == 0) index = 202
                        else if (top != 0 && bottom == 0 && left == 0 && right != 0) index = 203
                        else if (top != 0 && bottom == 0 && left != 0 && right == 0) index = 204
                        else if (top == 0 && bottom != 0 && left == 0 && right != 0) index = 205
                        else if (top == 0 && bottom != 0 && left != 0 && right == 0) index = 206
                    }

                    if (index != 0) {
                        grid[y][x] = index
                    }
                }
            }
        }

        private fun findStartCellInternal(grid: Array<IntArray>): Offset {
            var spawnRoom = Offset(x = grid[0].size.toFloat(), y = grid.size.toFloat())

            for (y: Int in 1 until grid.size - 1) {
                for (x: Int in 1 until grid[y].size - 1) {
                    if (grid[y][x] / 100 == 1) {
                        if (x < spawnRoom.x || y < spawnRoom.y) {
                            spawnRoom = Offset(x = x.toFloat(), y = y.toFloat())
                        }
                    }
                }
            }
            return spawnRoom
        }

    }

    val size: Int get() = height

    fun getTerrainAt(x: Int, y: Int): TerrainType {
        return when (grid[y][x] / 100) {
            1 -> TerrainType.ROAD
            2 -> TerrainType.GRASS
            else -> TerrainType.ABYSS
        }
    }

    fun isMovable(x: Int, y: Int): Boolean {
        return getTerrainAt(x, y) != TerrainType.ABYSS
    }

    fun getSpeedModifier(position: Offset): Float {
        val cellX: Int = position.x.toInt().coerceIn(0, size - 1)
        val cellY: Int = position.y.toInt().coerceIn(0, size - 1)

        return getTerrainAt(x = cellX, y = cellY).speedModifier
    }
}