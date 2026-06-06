package dev.galtyou.cardiolab.seleccion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.galtyou.cardiolab.core.model.CasoEcg
import dev.galtyou.cardiolab.core.model.MetadatosEcg
import dev.galtyou.cardiolab.core.theme.Paleta
import dev.galtyou.cardiolab.core.theme.paletaDe

@Composable
fun PantallaSeleccionCaso(
    temaOscuro: Boolean = true,
    viewModel: SeleccionViewModel = remember { SeleccionViewModel() },
    onCasoSeleccionado: (CasoEcg) -> Unit
) {
    val p = paletaDe(temaOscuro)
    val ui by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.fondo)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(
                text = "CardioLab",
                color = p.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Casos Clínicos Disponibles",
                color = p.text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Seleccione un registro electrocardiográfico para comenzar el análisis dinámico.",
                color = p.muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (ui.error != null) {
                Text(text = ui.error!!, color = p.lzMayor, fontSize = 14.sp)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ui.casos) { caso ->
                    TarjetaCaso(
                        caso = caso,
                        meta = ui.metadatos[caso.archivoBin],
                        paleta = p,
                        onClick = { onCasoSeleccionado(caso) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCaso(
    caso: CasoEcg,
    meta: MetadatosEcg?,
    paleta: Paleta,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(paleta.surface)
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meta?.diagnostico ?: "Cargando…",
                color = paleta.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (meta != null)
                    "Fichero: ${caso.archivoBin}  •  ${meta.derivaciones.size} derivaciones (${meta.fs} Hz)"
                else
                    "Fichero: ${caso.archivoBin}",
                color = paleta.muted,
                fontSize = 12.sp
            )
        }

        Text(
            text = "→",
            color = paleta.accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
