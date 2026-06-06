package dev.galtyou.cardiolab.core.theme

import androidx.compose.ui.graphics.Color

data class Paleta(
    val fondo: Color, val surface: Color, val text: Color, val muted: Color, val accent: Color,
    val lzFondo: Color, val lzMenor: Color, val lzMayor: Color, val lzOnda: Color
)

val paletaOscura = Paleta(
    Color(0xFF071318), Color(0xFF11272E), Color(0xFFE9F4F1), Color(0xFF8FB3C0), Color(0xFF2F847C),
    Color(0xFF0B1F26), Color(0x335DCAA5), Color(0x807FA8D8), Color(0xFF2F847C)
)

val paletaClara = Paleta(
    Color(0xFFF3EEF1), Color(0xFFFFFFFF), Color(0xFF1C1433), Color(0xFF6B6478), Color(0xFF2F847C),
    Color(0xFFFFF1F1), Color(0xFFEEA8A8), Color(0xFFD45F5F), Color(0xFF14202A)
)

fun paletaDe(temaOscuro: Boolean): Paleta = if (temaOscuro) paletaOscura else paletaClara
