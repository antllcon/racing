package com.mobility.race.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mobility.race.presentation.multiplayer.RoomViewModel

@Composable
fun RoomScreen(
    viewModel: RoomViewModel
) {
    val state = viewModel.state.value

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = state.roomId)
            Text(text = "Players:")

            for ((_, player) in state.playerNames)
            {
                Text(player)
            }

            if (state.isCreatingRoom && state.playerNames.size != 1) {
                Button(
                    onClick = {
                        viewModel.startGame()
                    }
                ) {
                    Text("Play")
                }
            }
        }
    }
}