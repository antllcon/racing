package com.mobility.race.presentation.multiplayer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.GameStopResponse
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.InfoResponse
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.LeftRoomResponse
import com.mobility.race.data.PlayerActionResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.PlayerDisconnectedResponse
import com.mobility.race.data.PlayerInputRequest
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.RoomUpdatedResponse
import com.mobility.race.data.Server
import com.mobility.race.data.ServerMessage
import com.mobility.race.data.StartedGameResponse
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.GameState
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

data class PlayerInput(
    val isAccelerating: Boolean = false,
    val turnDirection: Float = 0f
)

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _gateway: IGateway = Gateway(
        client = httpClient,
        serverConfig = Server.default(),
        onServerMessage = this::handleServerMessage,
        onError = this::handleGatewayError
    )

    private val _playerName: String = checkNotNull(savedStateHandle["playerName"])
    private val _roomName: String = checkNotNull(savedStateHandle["roomName"])
    private val _isCreatingRoom: Boolean = checkNotNull(savedStateHandle["isCreatingRoom"])

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isCanvasReady = MutableStateFlow(false)
    val isCanvasReady: StateFlow<Boolean> = _isCanvasReady.asStateFlow()

    private val _isViewModelReady = MutableStateFlow(false)
    val isViewModelReady: StateFlow<Boolean> = _isViewModelReady.asStateFlow()

    private var _gameLoopJob: Job? = null

    private val _playerInput = MutableStateFlow(PlayerInput())

    lateinit var car: Car
        private set

    lateinit var map: GameMap
        private set

    lateinit var camera: GameCamera
        private set

    private var _isGameStarted = false

    init {
        viewModelScope.launch {
            try {
                car =
                    Car(id = "TEMP_ID", playerName = _playerName, position = Offset(5f, 5f))
                map = GameMap.generateDungeonMap()
                camera = GameCamera(position = car.position, mapWidth = map.width, mapHeight = map.height)

//                _gameEngine = GameEngine(
//                    localPlayerId = car.id,
//                    map = map,
//                    camera = camera,
//                )
                _gameState.value = GameState(
                    players = listOf(car),
                    localPlayerId = car.id
                )

                _gateway.connect()
                delay(1000)
                _gateway.initPlayer(_playerName)
                delay(1000)

                if (_isCreatingRoom) {
                    _gateway.createRoom(_roomName)
                } else {
                    _gateway.joinRoom(_roomName)
                }
                delay(1000)

                _isViewModelReady.value = true

            } catch (e: Exception) {
                println("ViewModel: Error during initial setup: ${e.message}")
            }
        }
    }

    fun onCanvasSizeChanged(size: Size) {
        if (camera.viewportSize != size) {
            camera.viewportSize = size
            println("==== поставили размеры для камеры $size")
        }

        if (_isViewModelReady.value && !_isCanvasReady.value && size.width > 0 && size.height > 0) {
            _isCanvasReady.value = true
            runGame()
            println("==== канвас готов и запустили игровой цикл")
        }
    }

    private fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        println("Пустой метод")
    }

    private  fun runGame() {
        println(" ==== запуск runGame")
//
//        if (!isGameEngineInitialized()) {
//            logGameEngineNotInitialized()
//            return
//        }
//
//        if (isGameLoopRunning()) {
//            logGameLoopAlreadyRunning()
//            return
//        }

        startGameLoop()
    }

    private  fun movePlayer(touchCoordinates: Offset) {
        if (!_isViewModelReady.value || !::car.isInitialized) return

        val isAccelerating = touchCoordinates != Offset.Zero
        var turnDirection = 0f

        if (isAccelerating) {
            val worldTouchPos = camera.screenToWorld(touchCoordinates)
            val angle = atan2(
                worldTouchPos.y - car.position.y,
                worldTouchPos.x - car.position.x
            )
            // Разница с текущим направлением машины
            var diff = angle - car.direction

            // Нормализуем угол, чтобы он был в диапазоне [-PI, PI]
            while (diff > Math.PI) diff -= 2 * Math.PI.toFloat()
            while (diff < -Math.PI) diff += 2 * Math.PI.toFloat()

            if (abs(diff) < Math.PI.toFloat() / 2) {
                turnDirection = if (diff > 0) 1f else -1f
            }
        }

        _playerInput.value = PlayerInput(isAccelerating, turnDirection)

        viewModelScope.launch {
            _gateway.playerAction(
                PlayerInputRequest(isAccelerating, turnDirection)
            )
        }
    }

    fun handleServerMessage(message: ServerMessage) {
        viewModelScope.launch {
            when (message) {
                is PlayerConnectedResponse -> {


//                    if (message.playerName == _playerName && car.id == "TEMP_ID") {
//
//                        val updatedCar = car.copy(id = message.playerId)
//
//                        car = updatedCar
//
//                        camera = camera.update(car.position)
////                        _gameEngine.updatePlayerInstance(updatedCar)
////                        _gameEngine.updateLocalPlayerId(updatedCar.id)
//
////                        _gameState.value = _gameEngine.getCurrentState()
//
//                        val updatedPlayers = _gameState.value.players.map { player ->
//                            if (player.id == "TEMP_ID") {
//                                updatedCar
//                            } else {
//                                player
//                            }
//                        }
//
//                        _gameState.value = _gameState.value.copy(
//                            players = updatedPlayers,
//                            localPlayerId = updatedCar.id
//                        )
////                        _gameEngine.updateLocalPlayerId(updatedCar.id)
//                        println("ViewModel: Local player ID updated to: ${updatedCar.id}")
//                    } else {
//                        if (_gameState.value.players.none { it.id == message.playerId }) {
//                            val newCar = Car(
//                                id = message.playerId,
//                                playerName = message.playerName,
//                                isPlayer = false,
//                                isMultiplayer = true,
//                                position = Offset(0f, 0f)
//                            )
//                            _gameState.value =
//                                _gameState.value.copy(players = _gameState.value.players + newCar)
//                        }
//                    }
                }

                is PlayerDisconnectedResponse -> {
                    println("ViewModel: Player ${message.playerId} disconnected")
                    _gameState.value =
                        _gameState.value.copy(players = _gameState.value.players.filter { it.id != message.playerId })
                }

                is GameStateUpdateResponse -> {
//                    _gameEngine.applyServerState(message.players)

                    //  TODO: убрать
//                    val receivedPlayerState =
//                        message.players.find { it.id == _gameEngine.getCurrentState().localPlayerId }
//                    println("Client: Raw PlayerStateDto received: posX=${receivedPlayerState?.posX}, posY=${receivedPlayerState?.posY}")

//                    val updatedStateFromEngine = _gameEngine.getCurrentState()
//                    _gameState.value = updatedStateFromEngine
                }

                is InfoResponse -> {
                    println("ViewModel: Info: ${message.message}")
                }

                is ErrorResponse -> {
                    println("ViewModel: server send error: ${message.code}")
                }

                is RoomCreatedResponse -> {
                    println("ViewModel: Room ${message.roomId} created")
                }

                is JoinedRoomResponse -> {
                    println("ViewModel: Successfully joined room: ${message.roomId}")
                }

                is LeftRoomResponse -> {
                    println("ViewModel: Left room: ${message.roomId}")
                }

                is RoomUpdatedResponse -> {
                    println("ViewModel: Room ${message.roomId} updated")
                }

                is PlayerActionResponse -> {
                    println("ViewModel: Player action response: ${message.name}")
                }

                is GameCountdownUpdateResponse -> TODO()
                is GameStopResponse -> TODO()
                is StartedGameResponse -> TODO()
            }
        }
    }

    private fun stopGame() {
        println("ViewModel: try to leave room $_roomName")
        _gameLoopJob?.cancel()
        viewModelScope.launch {
            _gateway.leaveRoom()
        }
    }

//    private fun isGameEngineInitialized(): Boolean =
//        this::_gameEngine.isInitialized

    private fun isGameLoopRunning(): Boolean =
        _gameLoopJob?.isActive == true

    private fun logGameEngineNotInitialized() {
        println("ViewModel: GameEngine not init, cannot run game")
    }

    private fun logGameLoopAlreadyRunning() {
        println("ViewModel: Game loop is already running")
    }

    private fun startGameLoop() {
        _gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.currentTimeMillis()

            while (isActive) {
                val (deltaTime, newLastTime) = calculateDeltaTime(lastTime)
                lastTime = newLastTime
//                println(deltaTime)

                updateGameState(deltaTime)
                delay(16)
            }
        }
    }

    private fun calculateDeltaTime(lastTime: Long): Pair<Float, Long> {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 1000f
        return deltaTime to currentTime
    }

    private fun updateGameState(deltaTime: Float) {
        val playerInput = _playerInput.value
//        val newState = _gameEngine.update(deltaTime, playerInput)
//        _gameState.value = newState
    }

    fun handleGatewayError(errorMessage: String) {
        viewModelScope.launch {
            println("ViewModel: Gateway error: $errorMessage")
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel: disconnect Gateway and close HttpClient")
        _gameLoopJob?.cancel()
        viewModelScope.launch {
            _gateway.disconnect()
            httpClient.close()
        }
    }
}
