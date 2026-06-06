package dev.galtyou.cardiolab.ingreso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cardiolab.shared.generated.resources.Res
import cardiolab.shared.generated.resources.kobi_dormido
import dev.galtyou.cardiolab.core.ui.KobiMascota
import org.jetbrains.compose.resources.painterResource

@Composable
fun PantallaIngreso(
    temaOscuro: Boolean = true,
    onComenzar: () -> Unit
) {
    val fondo = if (temaOscuro) Color(0xFF071318) else Color(0xFFF3EEF1)
    val muted = if (temaOscuro) Color(0xFF8FB3C0) else Color(0xFF6B6478)
    val accent = Color(0xFF2F847C)
    val scrollState = rememberScrollState()

    val kobiDormidoPainter = painterResource(Res.drawable.kobi_dormido)

    Box(
        modifier = Modifier.fillMaxSize().background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 450.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                text = "CardioLab",
                color = accent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Análisis dinámico de electrocardiogramas",
                color = muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            KobiMascota(
                mensaje = "¡Hola! Te doy la bienvenida a CardioLab. ¿Listo para explorar los registros clínicos?",
                temaOscuro = temaOscuro,
                imagenKobi = kobiDormidoPainter
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onComenzar,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = "Ingresar a la Plataforma",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
