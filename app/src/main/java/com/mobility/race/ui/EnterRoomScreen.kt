package com.mobility.race.ui

import androidx.compose.foundation.layout.Box
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
    navigateToMultiplayer: (String) -> Unit
) {
    var name: String by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        TextField(
            value = name,
            onValueChange = { newName -> name = newName }
        )

        Button(onClick = { navigateToMultiplayer(name) }) {
            Text(text = "Enter game")
        }
    }
}