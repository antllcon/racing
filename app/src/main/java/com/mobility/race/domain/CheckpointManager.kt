package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset

class CheckpointManager(private val route: List<Offset>) {

    /**
     * Хранит состояние каждого гонщика
     * Ключ - car.id
     * Значение - индекс следующего чекпоинта car в списке route
     */
    private val carProgress: MutableMap<String, Int> = mutableMapOf()

    /**
     * Хранит количество пройденных кругов для каждой машины
     * Ключ - car.id
     * Значение - количество полных кругов
     */
    private val carLaps: MutableMap<String, Int> = mutableMapOf()

    /**
     * Регистрирует новую машину в гонке
     * Каждая машина начинает с 0 чекпоинта
     */
    fun registerCar(carId: String) {
        carProgress[carId] = 0
        carLaps[carId] = 0
    }

    /**
     * Возвращает позицию следующего чекпоинта для указанной машины
     */
    fun getNextCheckpoint(carId: String): Offset? {
        if (route.isEmpty()) {
            println("Warning: Route is empty, cannot get next checkpoint")
            return null
        }
        val nextCheckpointIndex: Int = carProgress[carId] ?: return null
        return route[nextCheckpointIndex]
    }

    /**
     * Обрабатывает достижение чекпоинта машиной
     * Если машина достигла правильного чекпоинта, ее цель обновляется на следующий
     * Если это был последний чекпоинт, засчитывается круг
     */
    fun onCheckpointReached(carId: String, reachedCheckpointPosition: Offset) {
        val nextCheckpointIndex: Int = carProgress[carId] ?: return
        val targetCheckpoint: Offset = route.getOrNull(nextCheckpointIndex) ?: return

        if (targetCheckpoint == reachedCheckpointPosition) {
            val newNextIndex: Int = (nextCheckpointIndex + 1)

            if (newNextIndex >= route.size) {
                carLaps[carId] = (carLaps[carId] ?: 0) + 1
                carProgress[carId] = 0
                println("Car $carId completed a lap! Total laps: ${carLaps[carId]}")
            } else {
                carProgress[carId] = newNextIndex
            }
        }
    }

    /**
     * Возвращает количество полных кругов для машины
     */
    fun getLapsForCar(carId: String): Int {
        return carLaps.getOrDefault(key = carId, defaultValue = 0)
    }
}