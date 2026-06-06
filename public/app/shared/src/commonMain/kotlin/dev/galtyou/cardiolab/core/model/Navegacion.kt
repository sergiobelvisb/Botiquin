package dev.galtyou.cardiolab.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Pantalla {
    @Serializable object Ingreso       : Pantalla()
    @Serializable object Login         : Pantalla()
    @Serializable object SeleccionCaso : Pantalla()
    @Serializable data class Visor(val caso: CasoEcg) : Pantalla()
}

@Serializable
data class CasoEcg(
    val archivoBin: String
)

@Serializable
data class MetadatosEcg(
    @SerialName("ecg_id")      val ecgId: Int,
    @SerialName("diagnostico") val diagnostico: String,
    @SerialName("fs")          val fs: Int,
    @SerialName("n_muestras")  val nMuestras: Int,
    @SerialName("derivaciones") val derivaciones: List<String>,
    @SerialName("hr_bpm")      val hrBpm: Int,
    @SerialName("fichero_bin") val ficheroBin: String
)