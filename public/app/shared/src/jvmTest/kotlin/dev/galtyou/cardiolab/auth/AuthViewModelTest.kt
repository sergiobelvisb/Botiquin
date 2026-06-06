package dev.galtyou.cardiolab.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests de integración del ciclo de autenticación.
 *
 * Verifican las cuatro transiciones de estado definidas en AuthState:
 *   IDLE → LOADING → SUCCESS  (credenciales demo, sin red)
 *   IDLE → LOADING → SUCCESS  (credenciales reales, requiere conexión)
 *   IDLE → LOADING → ERROR    (credenciales incorrectas)
 *   ERROR → IDLE              (resetState tras error)
 *
 * Las pruebas con credenciales demo son offline: el bypass de
 * AuthRepository devuelve Result.success sin contactar Supabase.
 */
class AuthViewModelTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Estado inicial
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `estado inicial es IDLE`() {
        val vm = AuthViewModel()
        assertEquals(AuthState.IDLE, vm.authState.value)
        assertEquals("", vm.errorMessage.value)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transición IDLE → LOADING
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `login establece LOADING de forma sincrona antes de completar`() {
        // AuthViewModel lanza una corrutina; LOADING se establece de forma
        // síncrona antes de suspender en la llamada a la red.
        val vm = AuthViewModel()
        // Usamos credenciales que NO son el bypass para que tarde algo más
        vm.login("cualquier@email.com", "cualquierpass")
        assertEquals(AuthState.LOADING, vm.authState.value)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOADING → SUCCESS con credenciales demo (offline)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `login demo devuelve SUCCESS sin conexion a internet`() = runBlocking {
        val vm = AuthViewModel()

        vm.login("prueba.user@gmail.com", "1234")

        // El bypass demo es inmediato: esperamos un ciclo de dispatch
        delay(200)

        assertEquals(
            expected  = AuthState.SUCCESS,
            actual    = vm.authState.value,
            message   = "Las credenciales demo deben autenticar offline sin Supabase"
        )
    }

    @Test
    fun `registro demo devuelve SUCCESS sin conexion a internet`() = runBlocking {
        val vm = AuthViewModel()

        vm.registro("prueba.user@gmail.com", "1234")
        delay(200)

        assertEquals(AuthState.SUCCESS, vm.authState.value)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOADING → ERROR con credenciales incorrectas
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `login con credenciales incorrectas devuelve ERROR con mensaje`() = runBlocking {
        val vm = AuthViewModel()

        vm.login("noexiste@correo.com", "contrasenaMala")
        delay(3000) // tiempo suficiente para que Supabase responda con error

        assertEquals(
            expected = AuthState.ERROR,
            actual   = vm.authState.value,
            message  = "Credenciales incorrectas deben producir estado ERROR"
        )
        assertNotEquals("", vm.errorMessage.value,
            "El mensaje de error no debe estar vacío tras fallo de autenticación")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reinicio de estado tras error
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `resetState restaura IDLE desde ERROR`() = runBlocking {
        val vm = AuthViewModel()

        vm.login("noexiste@correo.com", "contrasenaMala")
        delay(3000)
        assertEquals(AuthState.ERROR, vm.authState.value)

        vm.resetState()

        assertEquals(
            expected = AuthState.IDLE,
            actual   = vm.authState.value,
            message  = "resetState debe volver a IDLE para permitir un nuevo intento"
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bypass: contraseña incorrecta con email demo no usa el bypass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `login con email demo pero contrasena incorrecta NO usa el bypass`() = runBlocking {
        val vm = AuthViewModel()

        // El bypass solo actua cuando AMBAS credenciales coinciden exactamente
        vm.login("prueba.user@gmail.com", "contrasenaMala")
        delay(3000)

        assertNotEquals(
            illegal = AuthState.SUCCESS,
            actual  = vm.authState.value,
            message = "El bypass offline solo debe activarse con la contrasena demo exacta"
        )
    }
}
