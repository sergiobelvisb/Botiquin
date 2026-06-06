package dev.galtyou.cardiolab

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.galtyou.cardiolab.auth.AuthViewModel
import dev.galtyou.cardiolab.auth.PantallaAuth
import dev.galtyou.cardiolab.core.model.Pantalla
import dev.galtyou.cardiolab.ingreso.PantallaIngreso
import dev.galtyou.cardiolab.seleccion.PantallaSeleccionCaso
import dev.galtyou.cardiolab.visor.VisorIndividual
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val PantallaSaver = run {
    androidx.compose.runtime.saveable.Saver<Pantalla, String>(
        save = { Json.encodeToString(it) },
        restore = { try { Json.decodeFromString(it) } catch (_: Exception) { null } }
    )
}

@Composable
fun App() {
    var pantalla by rememberSaveable(stateSaver = PantallaSaver) {
        mutableStateOf<Pantalla>(Pantalla.Ingreso)
    }

    var temaOscuro by remember { mutableStateOf(true) }

    val authViewModel = remember { AuthViewModel() }

    when (val actual = pantalla) {
        is Pantalla.Ingreso -> {
            PantallaIngreso(
                temaOscuro = temaOscuro,
                onComenzar = { pantalla = Pantalla.Login } // Al pulsar el botón, salta al login
            )
        }

        is Pantalla.Login -> {
            PantallaAuth(
                viewModel = authViewModel,
                temaOscuro = temaOscuro,
                onAuthSuccess = { pantalla = Pantalla.SeleccionCaso } // Avanza al selector clínico
            )
        }

        is Pantalla.SeleccionCaso -> {
            PantallaSeleccionCaso(
                temaOscuro = temaOscuro,
                onCasoSeleccionado = { caso -> pantalla = Pantalla.Visor(caso) }
            )
        }

        is Pantalla.Visor -> {
            VisorIndividual(
                caso = actual.caso,
                temaOscuro = temaOscuro,
                onTemaToggle = { temaOscuro = !temaOscuro },
                onAtras = { pantalla = Pantalla.SeleccionCaso }
            )
        }
    }
}
