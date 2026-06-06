package dev.galtyou.cardiolab.auth

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CancellationException

class AuthRepository {
    private val USER_DEMO = "prueba.user@gmail.com"
    private val PASS_DEMO = "1234"

    suspend fun login(email: String, pass: String): Result<Unit> {

        if (email.trim() == USER_DEMO && pass == PASS_DEMO) {
            return Result.success(Unit)
        }

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            Result.failure(Exception("Credenciales erróneas"))
        }
    }

    suspend fun registro(email: String, pass: String): Result<Unit> {

        if (email.trim() == USER_DEMO && pass == PASS_DEMO) {
            return Result.success(Unit)
        }

        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            Result.failure(Exception("Error al registrarse. Comprueba tu correo e inténtalo de nuevo."))
        }
    }
}