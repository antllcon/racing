package com.mobility.race.data

import com.mobility.race.domain.GameMap
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable


class Gateway(
    private val client: HttpClient,
    private val serverConfig: Server,
) : IGateway {
    private var session: WebSocketSession? = null
    private var job: Job? = null
    private lateinit var gatewayStorage: StarterPack

    override val messageFlow: Flow<ServerMessage>
        get() = mMessageFlow.asSharedFlow()

    private val mMessageFlow = MutableSharedFlow<ServerMessage>()

    override fun fillGatewayStorage(starterPack: StarterPack) {
        gatewayStorage = starterPack
    }

    override fun openGatewayStorage(): StarterPack {
        return gatewayStorage
    }

    override suspend fun connect() {
        val protocolString = if (serverConfig.port == 443) "wss" else "ws"
//        println("Gateway: try connect to $protocolString://${serverConfig.host}:${serverConfig.port}${serverConfig.path}")

        try {
            session = client.webSocketSession(host = serverConfig.host, path = serverConfig.path) {
                url {
                    protocol = if (serverConfig.port == 443) URLProtocol.WSS else URLProtocol.WS
                    port = serverConfig.port
                }
            }
            job = startGettingMessages(session!!)

        } catch (_: Exception) {
//            println("Gateway: fail to connect server")
        }
    }

    override suspend fun disconnect() {
//        println("Gateway: try disconnect")

        try {
            job?.cancelAndJoin()
            session?.close()

        } catch (_: Exception) {
//            println("Gateway: Error during disconnect")

        } finally {
            session = null
            job = null
//            println("Gateway: disconnect from server")
        }
    }

    private suspend fun sendMessage(message: ClientMessage) {
//        println("Gateway: try to send message: $message")

        if (session?.isActive == true) {
            try {
                sendToSession(session!!, message)

            } catch (_: Exception) {
//                println("Gateway: fail to send message")
            }
        } else {
//            println("Gateway: Not connected or session inactive, cannot send message")
        }
    }

    override suspend fun initPlayer(name: String) {
//        println("Gateway: init player with name: $name")
        sendMessage(InitPlayerRequest(name = name))
    }

    override suspend fun createRoom(name: String) {
//        println("Gateway: create room with name: $name")
        sendMessage(CreateRoomRequest(name = name))
    }

    override suspend fun joinRoom(name: String) {
//        println("Gateway: join room with name: $name")
        sendMessage(JoinRoomRequest(name = name))
    }

    override suspend fun leaveRoom() {
//        println("Gateway: Sending LeaveRoomRequest.")
        sendMessage(LeaveRoomRequest)
    }

    override suspend fun startGame(name: String) {
        sendMessage(StartGameRequest(name))
    }

    override suspend fun playerAction(name: PlayerInputRequest) {
//        println("Gateway: player action with input: $name")
        sendMessage(name)
    }

    private fun startGettingMessages(session: WebSocketSession): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                for (frame in session.incoming) {
                    processIncomingFrame(frame)
                }
            } catch (e: Exception) {
                if (e is ClosedReceiveChannelException) {
//                    println("Gateway: WebSocket channel correctly closed")
                } else {
//                    println("Gateway: Error from WebSocket")
                }
            } finally {
//                println("Gateway: Job finished")
            }
        }
    }

    private suspend fun processIncomingFrame(frame: Frame) {
        if (frame is Frame.Text) {
//            println(frame.readText())
            val text = frame.readText()
            try {
                val serverMessage: ServerMessage =
                    AppJson.decodeFromString<ServerMessage>(string = text)
                mMessageFlow.emit(serverMessage)
            } catch (_: Exception) {
//                println("Gateway: Error deserializing server message")
            }
        }
    }
}