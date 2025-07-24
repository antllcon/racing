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
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.Server
import com.mobility.race.di.MultiplayerGameViewModelFactory
import com.mobility.race.di.RoomViewModelFactory
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.presentation.multiplayer.RoomViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
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
    val roomName: String
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

    val gateway: IGateway = Gateway(
        client = httpClient,
        serverConfig = Server.default()
    )

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
            val navigateToMultiplayer: () -> Unit = {
                navController.navigate(route = MultiplayerGame(args.playerName, args.roomName))
            }


            val factory = remember(gateway) {
                RoomViewModelFactory(args.playerName, args.roomName, args.isCreatingRoom, navigateToMultiplayer, gateway)
            }
            val viewModel: RoomViewModel = viewModel(factory = factory)

            RoomScreen(viewModel = viewModel)
        }

        composable<MultiplayerGame> { entry ->
            val args = entry.toRoute<MultiplayerGame>()

            println(" ===== Я в navHost - multiplayer")
            val factory = remember(gateway) {
                MultiplayerGameViewModelFactory(gateway)
            }

            val viewModel: MultiplayerGameViewModel = viewModel(factory = factory)

            MultiplayerGameScreen(viewModel)

//            MultiplayerGameScreen(
//                playerName = args.playerName,
//                roomName = args.roomName,
//                viewModel = viewModel
//            )
        }
    }
}