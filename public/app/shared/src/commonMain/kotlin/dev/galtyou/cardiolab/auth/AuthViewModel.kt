package dev.galtyou.cardiolab.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthState {
    IDLE, LOADING, SUCCESS, ERROR
}

/** Estado inmutable que la UI de autenticación renderiza (UDF). */
data class AuthUiState(
    val estado: AuthState = AuthState.IDLE,
    val error: String = ""
)

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _uiState.value = AuthUiState(estado = AuthState.ERROR, error = "Credenciales erróneas")
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)

    fun login(email: String, pass: String) {
        _uiState.value = AuthUiState(estado = AuthState.LOADING)
        scope.launch {
            try {
                val result = repository.login(email, pass)
                _uiState.value = if (result.isSuccess) {
                    AuthUiState(estado = AuthState.SUCCESS)
                } else {
                    AuthUiState(estado = AuthState.ERROR, error = result.exceptionOrNull()?.message ?: "Credenciales erróneas")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _uiState.value = AuthUiState(estado = AuthState.ERROR, error = "Credenciales erróneas")
            }
        }
    }

    fun registro(email: String, pass: String) {
        _uiState.value = AuthUiState(estado = AuthState.LOADING)
        scope.launch {
            try {
                val result = repository.registro(email, pass)
                _uiState.value = if (result.isSuccess) {
                    AuthUiState(estado = AuthState.SUCCESS)
                } else {
                    AuthUiState(estado = AuthState.ERROR, error = result.exceptionOrNull()?.message ?: "Error al registrarse")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _uiState.value = AuthUiState(estado = AuthState.ERROR, error = "Error al registrarse. Comprueba tu correo e inténtalo de nuevo.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
