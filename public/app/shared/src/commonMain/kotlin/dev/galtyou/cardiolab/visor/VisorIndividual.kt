package dev.galtyou.cardiolab.visor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.galtyou.cardiolab.core.model.CasoEcg
import dev.galtyou.cardiolab.core.theme.Paleta
import dev.galtyou.cardiolab.core.theme.paletaDe

private const val RANGO_MV = 2.0f
private val DERIVACIONES_DEFAULT = listOf("I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6")

fun calcularRrHr(senal: FloatArray, base: Float, fs: Int): Pair<Int, Int> {
    var maxPos = 0f
    for (v in senal) { val d = v - base; if (d > maxPos) maxPos = d }
    if (maxPos <= 0f) return 0 to 0
    val umbral = base + 0.6f * maxPos
    val refractario = fs / 5
    val picos = ArrayList<Int>()
    var i = 1
    while (i < senal.size - 1) {
        if (senal[i] > umbral && senal[i] >= senal[i-1] && senal[i] > senal[i+1]) {
            if (picos.isEmpty() || i - picos[picos.size-1] > refractario) picos.add(i)
        }
        i++
    }
    if (picos.size < 2) return 0 to 0
    var suma = 0
    for (k in 1 until picos.size) suma += picos[k] - picos[k-1]
    val rrMs = ((suma.toFloat() / (picos.size-1)) / fs * 1000f).toInt()
    return rrMs to (if (rrMs > 0) 60000 / rrMs else 0)
}

@Composable
fun LienzoEcg(muestras: FloatArray, inicio: Int, baseLine: Float, zoom: Float, despY: Float, p: Paleta) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = p.lzFondo)
        val mm = size.height / (20f * RANGO_MV) * zoom
        val pxPorMv = 10f * mm
        val pxPorMuestra = mm / 20f
        val eje = size.height / 2f + despY

        var x = 0f; var iv = 0
        while (x <= size.width) {
            val may = iv % 5 == 0
            drawLine(if (may) p.lzMayor else p.lzMenor, Offset(x, 0f), Offset(x, size.height), strokeWidth = if (may) 1.4f else 0.7f)
            x += mm; iv++
        }
        var y = eje; var ih = 0
        while (y >= 0f) {
            val may = ih % 5 == 0
            drawLine(if (may) p.lzMayor else p.lzMenor, Offset(0f, y), Offset(size.width, y), strokeWidth = if (may) 1.4f else 0.7f)
            y -= mm; ih++
        }
        y = eje + mm; ih = 1
        while (y <= size.height) {
            val may = ih % 5 == 0
            drawLine(if (may) p.lzMayor else p.lzMenor, Offset(0f, y), Offset(size.width, y), strokeWidth = if (may) 1.4f else 0.7f)
            y += mm; ih++
        }

        val visibles = (size.width / pxPorMuestra).toInt().coerceAtLeast(2)
        val fin = (inicio + visibles).coerceAtMost(muestras.size)
        val total = (fin - inicio).coerceAtLeast(2)
        val ruta = Path()
        for (j in 0 until total) {
            val v = muestras[inicio + j] - baseLine
            if (j == 0) ruta.moveTo(j * pxPorMuestra, eje - v * pxPorMv) else ruta.lineTo(j * pxPorMuestra, eje - v * pxPorMv)
        }
        drawPath(ruta, color = p.lzOnda.copy(alpha = 0.30f), style = Stroke(width = 7f))
        drawPath(ruta, color = p.lzOnda, style = Stroke(width = 2.5f))
    }
}

@Composable
private fun BotonControl(texto: String, fondo: Color, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(fondo).clickable { onClick() }.padding(vertical = 13.dp),
        contentAlignment = Alignment.Center) { Text(texto, color = color, fontWeight = FontWeight.Bold) }
}

@Composable
private fun TarjetaMetrica(titulo: String, valor: String, unidad: String, fondo: Color, cText: Color, cMuted: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(fondo).padding(12.dp)) {
        Text(titulo, color = cMuted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valor, color = cText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp)); Text(unidad, color = cMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Cabecera(
    p: Paleta, derivacion: Int, derivaciones: List<String>, expanded: Boolean, onExpand: () -> Unit, onDismiss: () -> Unit,
    onSelect: (Int) -> Unit, temaOscuro: Boolean, onTema: () -> Unit, onAtras: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("←", color = p.text, fontSize = 22.sp, modifier = Modifier.clickable { onAtras() })
        Spacer(Modifier.weight(1f))
        Box {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(p.surface).clickable { onExpand() }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("D ${derivaciones[derivacion]} ▾", color = p.text, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onDismiss() }) {
                derivaciones.forEachIndexed { i, nombre -> DropdownMenuItem(text = { Text(nombre) }, onClick = { onSelect(i) }) }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(p.surface).clickable { onTema() }, contentAlignment = Alignment.Center) {
            Text(if (temaOscuro) "☀" else "☾", color = p.accent, fontSize = 16.sp)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(p.accent), contentAlignment = Alignment.Center) { Text("K", color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun PanelControles(p: Paleta, reproduciendo: Boolean, velocidad: Float, zoom: Float, rrHr: Pair<Int, Int>,
                           onAtras: () -> Unit, onPlay: () -> Unit, onAdelante: () -> Unit,
                           onVel: (Float) -> Unit, onZoom: (Float) -> Unit, onReajustar: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BotonControl("Atrás", p.surface, p.text, Modifier.weight(1f)) { onAtras() }
        BotonControl(if (reproduciendo) "Pausa" else "Play", p.accent, Color.White, Modifier.weight(1.4f)) { onPlay() }
        BotonControl("Adelante", p.surface, p.text, Modifier.weight(1f)) { onAdelante() }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(1f to "1x", 0.5f to "0.5x", 0.25f to "0.25x").forEach { (v, etq) ->
            val act = velocidad == v
            BotonControl(etq, if (act) p.accent else p.surface, if (act) Color.White else p.muted, Modifier.weight(1f)) { onVel(v) }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Escala", color = p.text, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f)); Text("zoom ×${(zoom * 10).toInt() / 10f}", color = p.muted, fontSize = 12.sp)
    }
    Slider(value = zoom, onValueChange = { onZoom(it) }, valueRange = 0.4f..6f)
    BotonControl("Reajustar vista", p.surface, p.text, Modifier.fillMaxWidth()) { onReajustar() }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TarjetaMetrica("♥ FC", if (rrHr.second > 0) "${rrHr.second}" else "—", "bpm", p.surface, p.text, p.muted, Modifier.weight(1f))
        TarjetaMetrica("Int.R", if (rrHr.first > 0) "${rrHr.first}" else "—", "ms", p.surface, p.text, p.muted, Modifier.weight(1f))
        TarjetaMetrica("QT", "—", "ms", p.surface, p.text, p.muted, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BotonControl("Guardar", p.surface, p.text, Modifier.weight(1f)) { }
        BotonControl("Exportar", p.surface, p.text, Modifier.weight(1f)) { }
        BotonControl("Notas", p.surface, p.text, Modifier.weight(1f)) { }
    }
}

@Composable
private fun Monitor(modifier: Modifier, p: Paleta, lead: FloatArray, inicio: Int, baseLine: Float, zoom: Float, despY: Float,
                    onSize: (Float, Float) -> Unit, onTransform: (Float, Float, Float) -> Unit) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .onSizeChanged { onSize(it.width.toFloat(), it.height.toFloat()) }
        .pointerInput(Unit) { detectTransformGestures { _, pan, gz, _ -> onTransform(pan.x, pan.y, gz) } }) {
        LienzoEcg(lead, inicio, baseLine, zoom, despY, p)
    }
}

@Composable
fun VisorIndividual(
    caso: CasoEcg,
    temaOscuro: Boolean, // Recibido globalmente
    onTemaToggle: () -> Unit, // Notifica el cambio hacia arriba
    onAtras: () -> Unit
) {
    val vm = remember { VisorViewModel() }
    val ui by vm.uiState.collectAsState()
    LaunchedEffect(caso.archivoBin) { vm.cargar(caso) }

    var posicion by remember { mutableStateOf(0f) }
    var reproduciendo by remember { mutableStateOf(false) }
    var velocidad by remember { mutableStateOf(1f) }
    var zoom by remember { mutableStateOf(3f) }
    var despY by remember { mutableStateOf(0f) }
    var derivacion by remember { mutableStateOf(1) }
    var menuAbierto by remember { mutableStateOf(false) }
    var ancho by remember { mutableStateOf(0f) }
    var alto by remember { mutableStateOf(0f) }

    val p = paletaDe(temaOscuro)
    val metadatos = ui.metadatos
    val muestras = ui.muestras
    val fs          = metadatos?.fs ?: 500
    val n           = metadatos?.nMuestras ?: 5000
    val derivaciones = metadatos?.derivaciones ?: DERIVACIONES_DEFAULT
    val diagnostico = metadatos?.diagnostico ?: ""
    val segundosBarrido = 2.5f; val salto = 250

    val todo = muestras
    val lead = remember(todo, derivacion, n) {
        val d = todo ?: return@remember null
        val ini = derivacion * n
        if (ini >= 0 && ini + n <= d.size) d.copyOfRange(ini, ini + n) else null
    }
    val baseLine = remember(lead) { val d = lead; if (d == null) 0f else { var s = 0.0; for (v in d) s += v; (s / d.size).toFloat() } }
    val rrHr = remember(todo, fs, n) {
        val src = todo ?: return@remember 0 to 0
        if (2 * n > src.size) return@remember 0 to 0
        val d = src.copyOfRange(n, 2 * n)
        var s = 0.0; for (v in d) s += v; calcularRrHr(d, (s / d.size).toFloat(), fs)
    }
    val visibles = if (alto > 0f) { val mm = alto / (20f * RANGO_MV) * zoom; (ancho / (mm / 20f)).toInt().coerceIn(2, n) } else 1000

    LaunchedEffect(reproduciendo, lead) {
        if (reproduciendo && lead != null) {
            var ant = withFrameNanos { it }
            while (true) {
                withFrameNanos { ahora ->
                    val dt = (ahora - ant) / 1_000_000_000f; ant = ahora
                    val maxIni = (n - visibles).coerceAtLeast(0)
                    var nv = posicion + dt * (visibles / segundosBarrido) * velocidad
                    if (nv > maxIni) nv = 0f
                    posicion = nv
                }
            }
        }
    }

    val onSize: (Float, Float) -> Unit = { w, h -> ancho = w; alto = h }
    val onTransform: (Float, Float, Float) -> Unit = { px, py, gz ->
        val a = ancho; val al = alto
        if (a > 0f && al > 0f) {
            val mm = al / (20f * RANGO_MV) * zoom
            val vis = (a / (mm / 20f)).toInt().coerceIn(2, n)
            posicion = (posicion - px * (vis / a)).coerceIn(0f, (n - vis).coerceAtLeast(0).toFloat())
        }
        despY = (despY + py).coerceIn(-al, al)
        zoom = (zoom * gz).coerceIn(0.4f, 6f)
    }
    val maxIniBtn = { (n - visibles).coerceAtLeast(0).toFloat() }

    BoxWithConstraints(Modifier.fillMaxSize().background(p.fondo)) {
        val landscape = maxWidth > maxHeight
        val lead0 = lead

        if (!landscape) {
            val anchoC = if (maxWidth > 720.dp) 700.dp else maxWidth
            val altoLienzo = (maxHeight * 0.45f).coerceIn(200.dp, 360.dp)
            Column(Modifier.width(anchoC).align(Alignment.TopCenter).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Cabecera(
                    p = p,
                    derivacion = derivacion,
                    derivaciones = derivaciones,
                    expanded = menuAbierto,
                    onExpand = { menuAbierto = true },
                    onDismiss = { menuAbierto = false },
                    onSelect = { i -> derivacion = i; posicion = 0f; despY = 0f; menuAbierto = false },
                    temaOscuro = temaOscuro,
                    onTema = onTemaToggle,
                    onAtras = onAtras
                )
                Spacer(Modifier.height(16.dp))
                Text("DIAGNÓSTICO", color = p.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(diagnostico, color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (lead0 == null) Text(ui.error ?: "Cargando…", color = p.muted) else {
                    Monitor(Modifier.fillMaxWidth().height(altoLienzo), p, lead0, posicion.toInt(), baseLine, zoom, despY, onSize, onTransform)
                    Spacer(Modifier.height(12.dp))
                    PanelControles(p, reproduciendo, velocidad, zoom, rrHr,
                        { posicion = (posicion - salto).coerceIn(0f, maxIniBtn()) }, { reproduciendo = !reproduciendo }, { posicion = (posicion + salto).coerceIn(0f, maxIniBtn()) },
                        { v -> velocidad = v }, { z -> zoom = z }, { zoom = 3f; despY = 0f })
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Cabecera(
                    p = p,
                    derivacion = derivacion,
                    derivaciones = derivaciones,
                    expanded = menuAbierto,
                    onExpand = { menuAbierto = true },
                    onDismiss = { menuAbierto = false },
                    onSelect = { i -> derivacion = i; posicion = 0f; despY = 0f; menuAbierto = false },
                    temaOscuro = temaOscuro,
                    onTema = onTemaToggle,
                    onAtras = onAtras
                )
                Spacer(Modifier.height(8.dp))
                Text("DIAGNÓSTICO", color = p.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(diagnostico, color = p.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (lead0 == null) Text(ui.error ?: "Cargando…", color = p.muted) else {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        Monitor(Modifier.weight(1.6f).fillMaxHeight(), p, lead0, posicion.toInt(), baseLine, zoom, despY, onSize, onTransform)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                            PanelControles(p, reproduciendo, velocidad, zoom, rrHr,
                                { posicion = (posicion - salto).coerceIn(0f, maxIniBtn()) }, { reproduciendo = !reproduciendo }, { posicion = (posicion + salto).coerceIn(0f, maxIniBtn()) },
                                { v -> velocidad = v }, { z -> zoom = z }, { zoom = 3f; despY = 0f })
                        }
                    }
                }
            }
        }
    }
}
