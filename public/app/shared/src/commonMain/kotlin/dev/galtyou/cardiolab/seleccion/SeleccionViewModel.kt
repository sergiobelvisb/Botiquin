package dev.galtyou.cardiolab.seleccion

import dev.galtyou.cardiolab.core.data.CASOS_ECG
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

data class SeleccionUiState(
    val cargando: Boolean = true,
    val casos: List<CasoEcg> = CASOS_ECG,
    val metadatos: Map<String, MetadatosEcg> = emptyMap(),
    val error: String? = null
)

class SeleccionViewModel(private val repo: EcgRepository = EcgRepository()) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(SeleccionUiState())
    val uiState: StateFlow<SeleccionUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.value = SeleccionUiState(cargando = true)
        scope.launch {
            try {
                val mapa = mutableMapOf<String, MetadatosEcg>()
                CASOS_ECG.forEach { caso ->
                    runCatching { mapa[caso.archivoBin] = repo.metadatos(caso) }
                }
                _uiState.value = SeleccionUiState(cargando = false, metadatos = mapa)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _uiState.value = SeleccionUiState(cargando = false, error = "No se pudieron cargar los casos clínicos.")
            }
        }
    }
}
