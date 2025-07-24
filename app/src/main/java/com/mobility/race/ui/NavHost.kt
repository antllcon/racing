package com.mobility.race.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.data.AppJson
import com.mobility.race.presentation.MultiplayerGameViewModelFactory
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
object Menu

@Serializable
object SingleplayerGame

@Serializable
data class EnterRoom(
    val name: String
)

@Serializable
data class Room(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean
)

@Serializable
object MultiplayerMenuScreen

@Serializable

data class MultiplayerGame(
    val playerName: String,
    val roomName: String,
    @Contextual
    val viewModel: MultiplayerGameViewModel
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val httpClient = remember {
        HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(AppJson)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                navigateToSingleplayer = { navController.navigate(route = SingleplayerGame) },
                navigateToMultiplayerMenuScreen = { navController.navigate(route = MultiplayerMenuScreen)}
            )
        }

        composable<MultiplayerMenuScreen> {
            MultiplayerMenuScreen(
                navigateToJoinRoom = { playerName ->
                    navController.navigate(route = EnterRoom(playerName))
                    },
                navigateToCreateRoom = {  playerName, roomName ->
                    navController.navigate(route = Room(playerName, roomName, true))
                }
            )
        }

        composable<SingleplayerGame> {
            SingleplayerGameScreen()
        }

        composable<EnterRoom> { entry ->
            val args = entry.toRoute<EnterRoom>()

            EnterRoomScreen(playerName = args.name, navigateToRoom = { playerName, roomName ->
                navController.navigate(route = Room(playerName, roomName, false))
            })
        }

        composable<Room> { entry ->
            val args = entry.toRoute<Room>()

            val factory = remember(httpClient) {
                MultiplayerGameViewModelFactory(httpClient)
            }

            val viewModel: MultiplayerGameViewModel = viewModel(factory = factory)

            RoomScreen(playerName = args.playerName,
                roomName = args.roomName,
                isCreatingRoom = args.isCreatingRoom,
                viewModel = viewModel,
                navigateToMultiplayer = { playerName, roomName ->
                    navController.navigate(route = MultiplayerGame(playerName, roomName, viewModel))
                })
        }

        composable<MultiplayerGame> { entry ->
            val args = entry.toRoute<MultiplayerGame>()

            MultiplayerGameScreen(
                playerName = args.playerName,
                roomName = args.roomName,
                viewModel = args.viewModel
            )
        }
    }
}