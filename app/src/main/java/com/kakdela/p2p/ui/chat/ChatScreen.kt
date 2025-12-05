@Composable
fun ChatScreen(chatId: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<MessageEntity>() }

    Column {
        LazyColumn { /* отображение сообщений */ }
        Row {
            TextField(value = "", onValueChange = {})
            IconButton(onClick = { /* отправить текст */ }) {
                Icon(Icons.Default.Send, "Отправить")
            }
            IconButton(onClick = {
                // Выбор фото/видео/файла
                context.startActivity(Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                })
            }) {
                Icon(Icons.Default.AttachFile, "Файл")
            }
            VoiceMessageButton { file -> /* отправить голосовое */ }
        }
    }
}
