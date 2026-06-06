package dev.galtyou.cardiolab.core.data

import cardiolab.shared.generated.resources.Res
import dev.galtyou.cardiolab.core.model.CasoEcg
import dev.galtyou.cardiolab.core.model.MetadatosEcg
import kotlinx.serialization.json.Json

val CASOS_ECG: List<CasoEcg> = listOf(
    CasoEcg(archivoBin = "norm_1.bin"),
    CasoEcg(archivoBin = "afib_17.bin"),
    CasoEcg(archivoBin = "imi_8.bin"),
)


fun bytesAFloats(bytes: ByteArray): FloatArray {
    val m = FloatArray(bytes.size / 4)
    for (i in m.indices) {
        val p = i * 4
        val bits = (bytes[p].toInt() and 0xFF) or ((bytes[p + 1].toInt() and 0xFF) shl 8) or
                ((bytes[p + 2].toInt() and 0xFF) shl 16) or ((bytes[p + 3].toInt() and 0xFF) shl 24)
        m[i] = Float.fromBits(bits)
    }
    return m
}

class EcgRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheMeta = mutableMapOf<String, MetadatosEcg>()
    private val cacheMuestras = mutableMapOf<String, FloatArray>()

    suspend fun metadatos(caso: CasoEcg): MetadatosEcg {
        cacheMeta[caso.archivoBin]?.let { return it }
        val jsonNombre = caso.archivoBin.replace(".bin", ".json")
        val texto = Res.readBytes("files/ecg/$jsonNombre").decodeToString()
        val meta = json.decodeFromString<MetadatosEcg>(texto)
        cacheMeta[caso.archivoBin] = meta
        return meta
    }

    suspend fun muestras(caso: CasoEcg): FloatArray {
        cacheMuestras[caso.archivoBin]?.let { return it }
        val datos = bytesAFloats(Res.readBytes("files/ecg/${caso.archivoBin}"))
        cacheMuestras[caso.archivoBin] = datos
        return datos
    }
}
