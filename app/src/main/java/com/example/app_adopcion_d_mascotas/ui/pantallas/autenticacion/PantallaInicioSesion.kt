package com.example.app_adopcion_d_mascotas.ui.pantallas.autenticacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.User
import com.example.app_adopcion_d_mascotas.datos.modelos.UserRole
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.AuthViewModel
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloAutenticacion

@Composable
fun PantallaInicioSesion(
    modeloAutenticacion: ModeloAutenticacion = viewModel(),
    alIniciarSesion: (Usuario) -> Unit
) {
    // Variables de estado en español
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.ADOPTANTE) }
    var modoRegistro by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Logo de la aplicación
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AdoptaAmor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (modoRegistro) "Crea tu cuenta" else "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Si está en modo registro, pedimos nombre y rol
        if (modoRegistro) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo o del refugio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Rol: Adoptante o Refugio
            Text(
                text = "Tipo de perfil:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = rolSeleccionado == RolUsuario.ADOPTANTE,
                    onClick = { rolSeleccionado = RolUsuario.ADOPTANTE },
                    label = { Text("Adoptante") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = rolSeleccionado == RolUsuario.REFUGIO,
                    onClick = { rolSeleccionado = RolUsuario.REFUGIO },
                    label = { Text("Refugio") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mínimo 8 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Mensaje de error si falla algo
        modeloAutenticacion.mensajeError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Principal: Iniciar Sesión o Registrarse
        Button(
            onClick = {
                if (modoRegistro) {
                    modeloAutenticacion.registrarse(nombre, correo, contrasena, rolSeleccionado, alIniciarSesion)
                } else {
                    modeloAutenticacion.iniciarSesion(correo, contrasena, alIniciarSesion)
                }
            },
            enabled = !modeloAutenticacion.estaCargando,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (modeloAutenticacion.estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (modoRegistro) "Registrarme" else "Iniciar Sesión",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Alternar entre login y registro
        TextButton(
            onClick = {
                modoRegistro = !modoRegistro
            }
        ) {
            Text(
                text = if (modoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inicio de sesión con Google
        OutlinedButton(
            onClick = { modeloAutenticacion.iniciarSesionConGoogle(alIniciarSesion) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continuar con Google",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Acceso Rápido de Prueba (Demo)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { modeloAutenticacion.accesoRapidoDemo(RolUsuario.ADOPTANTE, alIniciarSesion) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Entrar Adoptante", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { modeloAutenticacion.accesoRapidoDemo(RolUsuario.REFUGIO, alIniciarSesion) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Entrar Refugio", fontSize = 12.sp)
            }
        }
    }
}
}
