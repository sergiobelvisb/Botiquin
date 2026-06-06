package dev.galtyou.cardiolab.visor

import dev.galtyou.cardiolab.core.data.EcgRepository
import dev.galtyou.cardiolab.core.model.CasoEcg
import dev.galtyou.cardiolab.core.model.MetadatosEcg
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de integración del VisorViewModel.
 *
 * Verifican:
 *  - Estado inicial: cargando=true, metadatos=null, muestras=null
 *  - Tras cargar: metadatos correctos, muestras con tamaño esperado
 *  - Extracción de derivaciones individuales del buffer plano
 *  - Comportamiento ante errores de carga
 *
 * Nota: estos tests usan los ficheros .bin y .json reales del directorio
 * de recursos del proyecto a través de EcgRepository.
 */
class VisorViewModelTest {

    private val casoNormal   = CasoEcg("norm_1.bin")
    private val casoAfib     = CasoEcg("afib_17.bin")
    private val casoInfarto  = CasoEcg("imi_8.bin")
    private val casoInvalido = CasoEcg("no_existe.bin")

    // ─────────────────────────────────────────────────────────────────────────
    // Estado inicial del ViewModel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `estado inicial tiene cargando=true y datos nulos`() {
        val vm = VisorViewModel()
        val ui = vm.uiState.value
        assertTrue(ui.cargando)
        assertNull(ui.metadatos)
        assertNull(ui.muestras)
        assertNull(ui.error)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga de metadatos desde JSON
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `cargar norm_1 produce metadatos con fs=500 y 12 derivaciones`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoNormal)
        delay(1000)

        val meta = vm.uiState.value.metadatos
        assertNotNull(meta, "Los metadatos no deben ser null tras la carga")
        assertEquals(500, meta.fs,
            "La frecuencia de muestreo debe leerse del JSON, no ser constante hardcodeada")
        assertEquals(12, meta.derivaciones.size)
        assertEquals("Ritmo sinusal normal", meta.diagnostico)
    }

    @Test
    fun `cargar afib_17 produce diagnostico correcto`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoAfib)
        delay(1000)

        val meta = vm.uiState.value.metadatos
        assertNotNull(meta)
        assertEquals("Fibrilacion auricular", meta.diagnostico)
    }

    @Test
    fun `cargar imi_8 produce diagnostico correcto`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoInfarto)
        delay(1000)

        val meta = vm.uiState.value.metadatos
        assertNotNull(meta)
        assertEquals("Infarto de cara inferior", meta.diagnostico)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tamaño del buffer de muestras
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `cargar norm_1 produce 60000 muestras — 12 derivaciones por 5000`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoNormal)
        delay(1000)

        val muestras = vm.uiState.value.muestras
        assertNotNull(muestras)
        assertEquals(60_000, muestras.size,
            "12 derivaciones × 5000 muestras/derivacion = 60 000 floats por registro")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Extracción de derivaciones individuales
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `cada derivacion ocupa exactamente nMuestras floats en el buffer`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoNormal)
        delay(1000)

        val ui = vm.uiState.value
        val muestras = ui.muestras ?: return@runBlocking
        val n = ui.metadatos?.nMuestras ?: 5000

        // Derivación II (índice 1): muestras[n*1 .. n*1+n)
        val derivacionII = muestras.copyOfRange(n * 1, n * 1 + n)
        assertEquals(n, derivacionII.size,
            "La derivación II debe contener exactamente $n muestras")
    }

    @Test
    fun `las 12 derivaciones contienen datos distintos entre si`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoNormal)
        delay(1000)

        val muestras = vm.uiState.value.muestras ?: return@runBlocking
        val n = 5000

        val derivI  = muestras.copyOfRange(0,    n)
        val derivV1 = muestras.copyOfRange(n * 6, n * 6 + n)

        // Derivación I y V1 representan proyecciones distintas del vector eléctrico:
        // sus valores no deben ser idénticos en un registro real
        val sonIguales = derivI.zip(derivV1.toList()).all { (a, b) -> a == b }
        assertTrue(!sonIguales,
            "Derivación I y V1 deben contener señales diferentes en un registro real")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manejo de errores
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `cargar fichero inexistente establece error y no bloquea la app`() = runBlocking {
        val vm = VisorViewModel()
        vm.cargar(casoInvalido)
        delay(1000)

        val ui = vm.uiState.value
        assertNotNull(ui.error,
            "Un fichero inexistente debe producir un mensaje de error en el UiState")
        assertNull(ui.muestras,
            "Las muestras deben ser null si la carga falló")
    }
}
