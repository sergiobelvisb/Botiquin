package dev.galtyou.cardiolab.visor

import dev.galtyou.cardiolab.core.data.bytesAFloats
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitarios para las funciones de parseo y análisis de señales ECG.
 *
 * Cubren:
 *  - bytesAFloats: conversión little-endian IEEE-754 float32 → FloatArray
 *  - calcularRrHr: detección de picos R y cálculo de FC e intervalo RR
 */
class EcgParserTest {

    // ─────────────────────────────────────────────────────────────────────────
    // bytesAFloats
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `bytesAFloats convierte correctamente el valor 1_0f en little-endian`() {
        // 1.0f en IEEE-754 little-endian = 0x00 0x00 0x80 0x3F
        val bytes = byteArrayOf(0x00, 0x00, 0x80.toByte(), 0x3F)
        val result = bytesAFloats(bytes)
        assertEquals(1, result.size)
        assertEquals(1.0, result[0].toDouble(), absoluteTolerance = 0.0001)
    }

    @Test
    fun `bytesAFloats convierte correctamente el valor 0_5f en little-endian`() {
        // 0.5f en IEEE-754 little-endian = 0x00 0x00 0x00 0x3F
        val bytes = byteArrayOf(0x00, 0x00, 0x00, 0x3F)
        val result = bytesAFloats(bytes)
        assertEquals(1, result.size)
        assertEquals(0.5, result[0].toDouble(), absoluteTolerance = 0.0001)
    }

    @Test
    fun `bytesAFloats parsea correctamente un array de cuatro floats`() {
        // Valores conocidos: 0.0f, 1.0f, -1.0f, 0.5f
        val bytes = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,                   // 0.0f
            0x00, 0x00, 0x80.toByte(), 0x3F,           // 1.0f
            0x00, 0x00, 0x80.toByte(), 0xBF.toByte(),  // -1.0f
            0x00, 0x00, 0x00, 0x3F                     // 0.5f
        )
        val result = bytesAFloats(bytes)
        assertEquals(4, result.size)
        assertEquals(0.0,  result[0].toDouble(), absoluteTolerance = 0.0001)
        assertEquals(1.0,  result[1].toDouble(), absoluteTolerance = 0.0001)
        assertEquals(-1.0, result[2].toDouble(), absoluteTolerance = 0.0001)
        assertEquals(0.5,  result[3].toDouble(), absoluteTolerance = 0.0001)
    }

    @Test
    fun `bytesAFloats devuelve FloatArray con tamano bytes_size dividido_4`() {
        val bytes = ByteArray(5000 * 4 * 12) { 0 } // 12 derivaciones × 5000 muestras
        val result = bytesAFloats(bytes)
        assertEquals(60_000, result.size)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calcularRrHr — señal sinusoidal sintética
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera una señal sinusoidal que simula una derivación II limpia
     * con una frecuencia cardíaca exacta de [bpm] latidos por minuto
     * a [fs] Hz de muestreo durante [segundos] segundos.
     */
    private fun senalSintetica(bpm: Int, fs: Int = 500, segundos: Int = 10): FloatArray {
        val n = fs * segundos
        val señal = FloatArray(n)
        val periodoPicos = (fs * 60.0 / bpm).toInt()
        for (i in señal.indices) {
            // Onda sinusoidal de amplitud 1.0 mV con la frecuencia del corazón
            señal[i] = sin(2.0 * PI * i / periodoPicos).toFloat()
        }
        return señal
    }

    @Test
    fun `calcularRrHr detecta FC correcta para 60 bpm`() {
        val fs = 500
        val señal = senalSintetica(bpm = 60, fs = fs)
        val base = 0f // línea base de la señal sinusoidal centrada en cero
        val (rrMs, fc) = calcularRrHr(señal, base, fs)

        assertTrue(fc in 55..65, "FC esperada ~60 bpm, obtenida: $fc bpm")
        assertTrue(rrMs in 900..1050, "RR esperado ~1000 ms, obtenido: $rrMs ms")
    }

    @Test
    fun `calcularRrHr detecta FC correcta para 75 bpm`() {
        val fs = 500
        val señal = senalSintetica(bpm = 75, fs = fs)
        val (rrMs, fc) = calcularRrHr(señal, 0f, fs)

        assertTrue(fc in 70..80, "FC esperada ~75 bpm, obtenida: $fc bpm")
        assertTrue(rrMs in 750..850, "RR esperado ~800 ms, obtenido: $rrMs ms")
    }

    @Test
    fun `calcularRrHr devuelve 0_0 para señal plana`() {
        val fs = 500
        val señalPlana = FloatArray(5000) { 0.0f }
        val (rrMs, fc) = calcularRrHr(señalPlana, 0f, fs)
        assertEquals(0, rrMs)
        assertEquals(0, fc)
    }

    @Test
    fun `calcularRrHr devuelve 0_0 con un solo pico`() {
        val fs = 500
        val señal = FloatArray(5000) { 0.0f }
        señal[250] = 2.0f // un único pico no permite calcular intervalo RR
        val (rrMs, fc) = calcularRrHr(señal, 0f, fs)
        assertEquals(0, rrMs)
        assertEquals(0, fc)
    }

    @Test
    fun `calcularRrHr respeta el periodo refractario de 200ms`() {
        val fs = 500
        val señal = FloatArray(5000) { 0.0f }
        // Dos picos muy juntos (50 muestras = 100 ms < periodo refractario 200 ms)
        // El segundo pico NO debe contarse como latido independiente
        señal[500] = 2.0f
        señal[550] = 2.0f  // 100 ms después — dentro del periodo refractario
        señal[1000] = 2.0f // 1000 ms después — fuera del refractario, sí cuenta
        val (_, fc) = calcularRrHr(señal, 0f, fs)
        // Solo hay 2 picos válidos (500 y 1000), no 3
        assertTrue(fc in 50..70, "FC esperada ~60 bpm ignorando pico refractario, obtenida: $fc bpm")
    }
}
