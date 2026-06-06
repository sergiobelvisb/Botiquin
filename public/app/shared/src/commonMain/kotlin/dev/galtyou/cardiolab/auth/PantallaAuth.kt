package dev.galtyou.cardiolab.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.galtyou.cardiolab.core.theme.paletaDe
import dev.galtyou.cardiolab.core.ui.KobiMascota

import cardiolab.shared.generated.resources.Res
import cardiolab.shared.generated.resources.kobi_carpeta
import cardiolab.shared.generated.resources.kobi_saludando
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAuth(
    viewModel: AuthViewModel,
    temaOscuro: Boolean = true,
    onAuthSuccess: () -> Unit
) {
    val p = paletaDe(temaOscuro)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var modoRegistro by remember { mutableStateOf(false) }

    val ui by viewModel.uiState.collectAsState()


    val kobiCarpeta = painterResource(Res.drawable.kobi_carpeta)
    val kobiSaludando = painterResource(Res.drawable.kobi_saludando)
    val scrollState = rememberScrollState()

    LaunchedEffect(ui.estado) {
        if (ui.estado == AuthState.SUCCESS) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(p.fondo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                text = if (modoRegistro) "Crear Cuenta" else "Iniciar Sesión",
                color = p.text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))


            val mensajeKobi = if (modoRegistro) {
                "¡Únete a CardioLab para empezar a aprender ECG!"
            } else {
                "Puedes usar la cuenta de pruebas:\nprueba.user@gmail.com (1234)"
            }
            val imagenActual = if (modoRegistro) kobiSaludando else kobiCarpeta

            KobiMascota(
                mensaje = mensajeKobi,
                temaOscuro = temaOscuro,
                imagenKobi = imagenActual
            )

            Spacer(modifier = Modifier.height(20.dp))


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.accent,
                    unfocusedBorderColor = p.surface,
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedLabelColor = p.accent,
                    unfocusedLabelColor = p.muted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.accent,
                    unfocusedBorderColor = p.surface,
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedLabelColor = p.accent,
                    unfocusedLabelColor = p.muted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (ui.estado == AuthState.LOADING) {
                CircularProgressIndicator(color = p.accent)
            } else {
                Button(
                    onClick = {
                        if (modoRegistro) viewModel.registro(email, password)
                        else viewModel.login(email, password)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = if (modoRegistro) "Registrarse" else "Iniciar sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (modoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                color = p.accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable {
                        modoRegistro = !modoRegistro
                        viewModel.resetState()
                    }
                    .padding(8.dp)
            )

            if (ui.estado == AuthState.ERROR) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ui.error,
                    color = Color(0xFFD45F5F),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}