package com.mobility.race.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.presentation.MultiplayerGameViewModel
import com.mobility.race.presentation.SingleplayerGameViewModel
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewmodel.compose.viewModel

@Serializable
object Menu

@Serializable
object SingleplayerGame

@Serializable
object EnterRoom

@Serializable
data class MultiplayerGame(
    val roomName: String,
    val playerName: String
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                navigateToSingleplayer = { navController.navigate(route = SingleplayerGame) },
                navigateToJoinRoom = { navController.navigate(route = EnterRoom) }
            )
        }

        composable<SingleplayerGame> {
            val viewModel = SingleplayerGameViewModel()

            SingleplayerGameScreen(viewModel)
        }

        composable<EnterRoom> {
            EnterRoomScreen(navigateToMultiplayer = { roomName, playerName ->
                navController.navigate(route = MultiplayerGame(roomName, playerName))
            })
        }

        composable<MultiplayerGame> { entry ->
            val roomName: String = entry.toRoute<MultiplayerGame>().roomName
            val playerName: String = entry.toRoute<MultiplayerGame>().playerName
            val viewModel = MultiplayerGameViewModel()

            MultiplayerGameScreen(
                roomName = roomName,
                playerName = playerName,
                viewModel = viewModel
            )
        }
    }
}