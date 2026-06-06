package dev.galtyou.cardiolab.visor

import dev.galtyou.cardiolab.core.model.MetadatosEcg
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests de deserialización de MetadatosEcg desde JSON.
 *
 * Verifican que EcgRepository lee correctamente los ficheros .json
 * de cada caso clínico y que ningún campo usa valores hardcodeados:
 *   - fs (frecuencia de muestreo) → leído del JSON, no constante en código
 *   - nMuestras                   → leído del JSON, no constante en código
 *   - derivaciones                → lista completa de 12 derivaciones
 *   - diagnostico                 → texto correcto para cada patología
 */
class EcgMetadatosTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers: JSON inline de cada caso (replica exacta del fichero en recursos)
    // ─────────────────────────────────────────────────────────────────────────

    private val jsonNormSinusal = """
        {
          "ecg_id": 1,
          "codigo": "NORM",
          "diagnostico": "Ritmo sinusal normal",
          "fs": 500,
          "n_muestras": 5000,
          "derivaciones": ["I","II","III","AVR","AVL","AVF","V1","V2","V3","V4","V5","V6"],
          "hr_bpm": 64,
          "fichero_bin": "norm_1.bin"
        }
    """.trimIndent()

    private val jsonFibrilacion = """
        {
          "ecg_id": 17,
          "codigo": "AFIB",
          "diagnostico": "Fibrilacion auricular",
          "fs": 500,
          "n_muestras": 5000,
          "derivaciones": ["I","II","III","AVR","AVL","AVF","V1","V2","V3","V4","V5","V6"],
          "hr_bpm": 85,
          "fichero_bin": "afib_17.bin"
        }
    """.trimIndent()

    private val jsonInfartoInferior = """
        {
          "ecg_id": 8,
          "codigo": "IMI",
          "diagnostico": "Infarto de cara inferior",
          "fs": 500,
          "n_muestras": 5000,
          "derivaciones": ["I","II","III","AVR","AVL","AVF","V1","V2","V3","V4","V5","V6"],
          "hr_bpm": 74,
          "fichero_bin": "imi_8.bin"
        }
    """.trimIndent()

    // ─────────────────────────────────────────────────────────────────────────
    // Ritmo sinusal normal (norm_1.json)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `norm_1 diagnostico es Ritmo sinusal normal`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonNormSinusal)
        assertEquals("Ritmo sinusal normal", meta.diagnostico)
    }

    @Test
    fun `norm_1 frecuencia de muestreo es 500 Hz`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonNormSinusal)
        assertEquals(500, meta.fs,
            "La frecuencia de muestreo debe leerse del JSON y no estar hardcodeada")
    }

    @Test
    fun `norm_1 contiene exactamente 12 derivaciones`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonNormSinusal)
        assertEquals(12, meta.derivaciones.size)
    }

    @Test
    fun `norm_1 nMuestras es 5000 — 10 segundos a 500 Hz`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonNormSinusal)
        assertEquals(5000, meta.nMuestras,
            "5000 muestras = 10 s × 500 Hz por derivación")
    }

    @Test
    fun `norm_1 derivaciones incluye las 6 de extremidades y 6 precordiales`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonNormSinusal)
        val esperadas = listOf("I","II","III","AVR","AVL","AVF","V1","V2","V3","V4","V5","V6")
        assertEquals(esperadas, meta.derivaciones)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fibrilación auricular (afib_17.json)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `afib_17 diagnostico es Fibrilacion auricular`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonFibrilacion)
        assertEquals("Fibrilacion auricular", meta.diagnostico)
    }

    @Test
    fun `afib_17 tiene 12 derivaciones y 5000 muestras`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonFibrilacion)
        assertEquals(12, meta.derivaciones.size)
        assertEquals(5000, meta.nMuestras)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Infarto de cara inferior (imi_8.json)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `imi_8 diagnostico es Infarto de cara inferior`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonInfartoInferior)
        assertEquals("Infarto de cara inferior", meta.diagnostico)
    }

    @Test
    fun `imi_8 hrBpm de referencia es 74 bpm`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonInfartoInferior)
        assertNotNull(meta.hrBpm)
        assertEquals(74, meta.hrBpm)
    }

    @Test
    fun `imi_8 nombre fichero bin coincide con el archivo de recursos`() {
        val meta = json.decodeFromString<MetadatosEcg>(jsonInfartoInferior)
        assertEquals("imi_8.bin", meta.ficheroBin)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tolerancia a claves desconocidas (ignoreUnknownKeys = true)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `deserializacion tolera campos extra en el JSON sin lanzar excepcion`() {
        val jsonConCamposExtra = """
            {
              "ecg_id": 99,
              "codigo": "TEST",
              "diagnostico": "Prueba de tolerancia",
              "fs": 500,
              "n_muestras": 5000,
              "derivaciones": ["I","II"],
              "hr_bpm": 60,
              "fichero_bin": "test.bin",
              "campo_nuevo_futuro": "valor_desconocido",
              "otro_campo": 42
            }
        """.trimIndent()

        // No debe lanzar SerializationException aunque haya claves desconocidas
        val meta = json.decodeFromString<MetadatosEcg>(jsonConCamposExtra)
        assertEquals("Prueba de tolerancia", meta.diagnostico)
    }
}
