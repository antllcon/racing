package com.mobility.race.data

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json

class Gateway(
    private val client: HttpClient,
    private val json: Json,
    private val serverHost: String = "localhost",
    private val serverPort: Int = 8080,
    private val serverPath: String = "/",
    private val onServerMessage: (ServerMessage) -> Unit,
    private val onError: (String) -> Unit
) {
    private var session: WebSocketSession? = null
    private var job: Job? = null

    suspend fun connect() {
        println("Gateway: Try connecting to ws://$serverHost:$serverPort$serverPath")

        try {
            session = setWebSocketSession()
            println("Gateway: Connected to WebSocket server")
            job = startGettingMessages(session!!)
        } catch (e: Exception) {
            onError("Failed to connect to server: ${e.message}")
            println("Gateway: Failed to connect to server")
        }
    }

    suspend fun disconnect() {
        println("Gateway: try disconnecting")
    }

    private suspend fun sendMessage(message: ClientMessage) {
        println("Gateway: trying to send message: $message")
    }

    private fun initPlayer(name: String) {
        println("Gateway: init player with name: $name")
    }

    suspend fun createRoom(name: String) {
        println("Gateway: create room with name: $name")
    }

    suspend fun joinRoom(name: String) {
        println("Gateway: join room with name: $name")
    }

    suspend fun leaveRoom() {
        println("Gateway: leave room")
    }

    suspend fun playerAction(name: String) {
        println("Gateway: player action with name: $name")
    }

    private suspend fun setWebSocketSession(): WebSocketSession {
        return client.webSocketSession(host = serverHost, port = serverPort, path = serverPath)
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

    private suspend fun processIncomingFrame(frame: Frame) {
        if (frame is Frame.Text) {
            val text = frame.readText()
//            println("Gateway: get raw: $text")
            try {
                val serverMessage = json.decodeFromString<ServerMessage>(text)
                onServerMessage(serverMessage)
            } catch (e: Exception) {
                onError("Error Deserializing server message: ${e.message}")
                println("Gateway: Error deserializing server message")
            }
        }
    }
}