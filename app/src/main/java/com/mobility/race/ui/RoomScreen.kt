package com.mobility.race.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.Server
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import io.ktor.client.HttpClient

@Composable
fun RoomScreen(
    playerName: String,
    roomName: String,
    isCreatingRoom: Boolean,
    viewModel: MultiplayerGameViewModel,
    navigateToMultiplayer: (String, String) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

        }
    }
}