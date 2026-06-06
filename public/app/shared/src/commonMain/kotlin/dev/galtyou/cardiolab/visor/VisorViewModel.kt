package dev.galtyou.cardiolab.visor

import dev.galtyou.cardiolab.core.data.EcgRepository
import dev.galtyou.cardiolab.core.model.CasoEcg
import dev.galtyou.cardiolab.core.model.MetadatosEcg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado inmutable del visor (UDF). Solo contiene los datos cargados desde disco;
 * la interacción (zoom, paneo, reproducción, derivación) es estado de UI local del composable.
 */
data class VisorUiState(
    val cargando: Boolean = true,
    val metadatos: MetadatosEcg? = null,
    val muestras: FloatArray? = null,
    val error: String? = null
)

class VisorViewModel(private val repo: EcgRepository = EcgRepository()) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(VisorUiState())
    val uiState: StateFlow<VisorUiState> = _uiState.asStateFlow()

    fun cargar(caso: CasoEcg) {
        _uiState.value = VisorUiState(cargando = true)
        scope.launch {
            try {
                val meta = repo.metadatos(caso)
                val mues = repo.muestras(caso)
                _uiState.value = VisorUiState(cargando = false, metadatos = meta, muestras = mues)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _uiState.value = VisorUiState(cargando = false, error = "No se pudo cargar el registro.")
            }
        }
    }
}
