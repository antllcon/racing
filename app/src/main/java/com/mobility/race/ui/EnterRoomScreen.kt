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

@Composable
fun EnterRoomScreen(
    playerName: String,
    navigateToRoom: (String, String) -> Unit
) {
    var roomName: String by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Enter code")

            TextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room name") }
            )

            Button(
                onClick = {
                    if (playerName.isNotBlank() && roomName.isNotBlank()) {
                        navigateToRoom(playerName, roomName)
                    }
                },
                enabled = playerName.isNotBlank() && roomName.isNotBlank()
            ) {
                Text("Join room")
            }
        }
    }
}
