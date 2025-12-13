package com.kakdela.p2p.ui.components

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun NeonTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = Color(0xFF00FFF0), fontWeight = FontWeight.Bold, fontSize = 16.sp),
        modifier = modifier
    )
}
