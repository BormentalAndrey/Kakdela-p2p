package com.kakdela.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kakdela.p2p.ui.ChatViewModel
import com.kakdela.p2p.ui.theme.KakDelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KakDelaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: ChatViewModel = viewModel()
                    ChatScreen(vm = vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel) {
    val messages by vm.messages.collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Как дела? P2P") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
                items(messages) { msg ->
                    Text("[${msg.senderId}]: ${msg.body}", modifier = Modifier.padding(4.dp))
                }
            }

            Row(modifier = Modifier.padding(8.dp)) {
                TextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { vm.sendMessage(text); text = "" }) { Text("Send") }
            }
        }
    }
}
