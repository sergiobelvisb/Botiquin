package dev.galtyou.cardiolab.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KobiMascota(
    mensaje: String,
    temaOscuro: Boolean,
    imagenKobi: Painter
) {
    val surface = if (temaOscuro) Color(0xFF11272E) else Color(0xFFFFFFFF)
    val text = if (temaOscuro) Color(0xFFE9F4F1) else Color(0xFF1C1433)
    val accent = Color(0xFF2F847C)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp))
            .background(surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = mensaje,
            color = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(if (temaOscuro) Color(0xFF11272E) else Color(0xFFE2ECE9)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = imagenKobi,
            contentDescription = "Mascota Kobi",
            modifier = Modifier.fillMaxSize().padding(8.dp)
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "KOBI",
        color = accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}
