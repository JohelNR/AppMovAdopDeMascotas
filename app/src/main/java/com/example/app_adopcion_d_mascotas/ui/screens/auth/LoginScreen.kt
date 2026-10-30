package com.example.app_adopcion_d_mascotas.ui.screens.auth

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
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole
import com.example.app_adopcion_d_mascotas.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit
) {
    // Variables de estado en español para que sean fáciles de leer y explicar
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(UserRole.ADOPTANTE) }
    var modoRegistro by remember { mutableStateOf(false) }
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }
    var mensajeRecuperacion by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo de la aplicación
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Logo Huellitas",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Adopta Huellitas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (modoRegistro) "Crea tu cuenta en la nube" else "Inicia sesión para continuar",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Si estamos en modo registro, pedimos el nombre
        if (modoRegistro) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre o Nombre del Refugio") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Campo de Correo Electrónico
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mínimo 8 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Rol si estamos registrando
        if (modoRegistro) {
            Text(
                text = "¿Cuál es tu rol?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = rolSeleccionado == UserRole.ADOPTANTE,
                    onClick = { rolSeleccionado = UserRole.ADOPTANTE },
                    label = { Text("🏠 Soy Adoptante") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = rolSeleccionado == UserRole.REFUGIO,
                    onClick = { rolSeleccionado = UserRole.REFUGIO },
                    label = { Text("🐾 Soy Refugio") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mensaje de error si la autenticación falla
        authViewModel.errorMessage?.let { mensajeError ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botón Principal (Entrar o Crear Cuenta)
        Button(
            onClick = {
                if (modoRegistro) {
                    authViewModel.register(nombre, correo, contrasena, rolSeleccionado, onLoginSuccess)
                } else {
                    authViewModel.login(correo, contrasena, onLoginSuccess)
                }
            },
            enabled = !authViewModel.isLoading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (modoRegistro) "Crear Cuenta" else "Entrar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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

        // Recuperar contraseña
        if (!modoRegistro) {
            TextButton(
                onClick = { mostrarDialogoRecuperar = true }
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña? Recupérala aquí",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inicio de sesión con Google
        OutlinedButton(
            onClick = { authViewModel.loginWithGoogle(onLoginSuccess) },
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
                onClick = { authViewModel.loginDemo(UserRole.ADOPTANTE, onLoginSuccess) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Entrar Adoptante", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { authViewModel.loginDemo(UserRole.REFUGIO, onLoginSuccess) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Entrar Refugio", fontSize = 12.sp)
            }
        }
    }

    // Ventana emergente para Recuperar Contraseña
    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRecuperar = false
                mensajeRecuperacion = null
            },
            title = {
                Text("Recuperar Contraseña", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo registrado para recibir las instrucciones de restablecimiento:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    mensajeRecuperacion?.let { mensaje ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (correoRecuperacion.isNotBlank()) {
                            mensajeRecuperacion = "¡Enlace de recuperación enviado con éxito a $correoRecuperacion!"
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogoRecuperar = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
