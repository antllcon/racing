package com.mobility.race.data

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

class Gateway(
    private val client: HttpClient,
    private val serverHost: String = "thecorpus.ru",
    private val serverPort: Int = 8080,
    private val serverPath: String = "/",
    private val onServerMessage: (ServerMessage) -> Unit,
    private val onError: (String) -> Unit
) {
    private var session: WebSocketSession? = null
    private var job: Job? = null

    suspend fun connect() {
        println("Gateway: Try connect to wss://$serverHost:$serverPort$serverPath...")

        try {
            println("зашел сюда")
            session = setWebSocketSession()
            println("Gateway: Connected to WebSocket server")
            job = startGettingMessages(session!!)

        } catch (e: Exception) {
            onError("Failed to connect to server: ${e.message}")
            println("Gateway: Failed to connect to server")
        }
    }

    suspend fun disconnect() {
        println("Gateway: Try disconnect...")

        try {
            job?.cancelAndJoin()
//            println("Gateway: Job cancelled and joined")
            session?.close()
//            println("Gateway: WebSocket session closed")

        } catch (e: Exception) {
            onError("Error during disconnect: ${e.message}")
            println("Gateway: Error during disconnect")
        } finally {
            session = null
            job = null
            println("Gateway: Disconnected from WebSocket server")
        }
    }

    private suspend fun sendMessage(message: ClientMessage) {
        println("Gateway: trying to send message: $message...")

        if (session?.isActive == true) {
            try {
                sendToSession(session!!, message)
                println("Gateway: Sent message")

            } catch (e: Exception) {
                onError("Failed to send message: ${e.message}")
                println("Gateway: Failed to send message")
            }
        } else {
            onError("Gateway is not connected or session is inactive. Cannot send message: $message")
            println("Gateway: Not connected or session inactive, cannot send message")
        }
    }

    suspend fun initPlayer(name: String) {
        // Может использоваться для локальной инициализации игровой логики,
        // которая не зависит от сетевого подключения
        println("Gateway: init player with name: $name")
        sendMessage(InitPlayerRequest(name = name))
    }

    suspend fun createRoom(name: String) {
        println("Gateway: create room with name: $name")
        sendMessage(CreateRoomRequest(name = name))
    }

    suspend fun joinRoom(name: String) {
        println("Gateway: join room with name: $name")
        sendMessage(JoinRoomRequest(name = name))
    }

    suspend fun leaveRoom() {
        println("Gateway: Sending LeaveRoomRequest.")
        sendMessage(LeaveRoomRequest)
    }

    suspend fun playerAction(name: String) {
        println("Gateway: player action with name: $name")
        sendMessage(JoinRoomRequest(name = name))
    }

    private suspend fun setWebSocketSession(): WebSocketSession {
        return client.webSocketSession(host = serverHost, path = serverPath) {
            url {
                protocol = URLProtocol.WSS
                port = serverPort
            }
        }
    }

    private fun startGettingMessages(session: WebSocketSession): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                for (frame in session.incoming) {
                    processIncomingFrame(frame)
                }
            } catch (e: Exception) {
                if (e is ClosedReceiveChannelException) {
                    println("Gateway: WebSocket channel correctly closed")
                } else {
                    onError("Error from WebSocket: ${e.message}")
                    println("Gateway: Error from WebSocket")
                }
            } finally {
                println("Gateway: Job finished")
            }
        }
    }

    private fun processIncomingFrame(frame: Frame) {
        if (frame is Frame.Text) {
            val text = frame.readText()
//            println("Gateway: get raw: $text")
            try {
                val serverMessage: ServerMessage =
                    AppJson.decodeFromString<ServerMessage>(string = text)
                onServerMessage(serverMessage)
            } catch (e: Exception) {
                onError("Error Deserializing server message: ${e.message}")
                println("Gateway: Error deserializing server message")
            }
        }
    }
}