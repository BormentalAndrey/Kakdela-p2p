package com.kakdela.p2p.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.ui.theme.NeonCyan
import com.kakdela.p2p.ui.theme.NeonPink

@Composable
fun MessageBubble(
    text: String,
    isSentByMe: Boolean,
    timestamp: String = "12:34"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(20.dp))
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(if (isSentByMe) NeonCyan else NeonPink.copy(alpha = 0.15f))
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = if (isSentByMe) Color.Black else Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timestamp,
                color = if (isSentByMe) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}
