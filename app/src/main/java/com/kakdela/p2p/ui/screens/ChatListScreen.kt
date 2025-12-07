package com.kakdela.p2p.ui.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatListScreen() {
    var counter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Привет! Нажмите кнопку:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { counter++ }) {
            Text("Нажми меня")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Нажато: $counter раз")
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListPreview() {
    ChatListScreen()
}
