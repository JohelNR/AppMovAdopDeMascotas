package com.example.app_adopcion_d_mascotas.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_adopcion_d_mascotas.data.model.User
import com.example.app_adopcion_d_mascotas.data.model.UserRole

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit
) {
    // 1. Estados en memoria RAM con remember
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.ADOPTANTE) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono y Título
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = "Logo",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Adopta Huellitas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (isRegisterMode) "Crea tu cuenta" else "Inicia sesión para continuar",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Selector de Rol (RF1: Adoptante vs Refugio)
        Text(
            text = "¿Cuál es tu rol?",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedRole == UserRole.ADOPTANTE,
                onClick = { selectedRole = UserRole.ADOPTANTE },
                label = { Text("🐾 Adoptante") },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedRole == UserRole.REFUGIO,
                onClick = { selectedRole = UserRole.REFUGIO },
                label = { Text("🏠 Refugio") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Campo de Correo Electrónico
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Correo Electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Mensaje de error si falla la validación
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Botón Principal (Login o Registro)
        Button(
            onClick = {
                // Validación básica de formato
                if (!email.contains("@") || email.isBlank()) {
                    errorMessage = "Por favor ingresa un correo válido con @"
                } else if (password.length < 6) {
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                } else {
                    // Éxito: creamos el usuario simulado con el rol elegido
                    val user = User(
                        id = "user_01",
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email,
                        role = selectedRole
                    )
                    onLoginSuccess(user)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(if (isRegisterMode) "Registrarse" else "Ingresar")
        }

        // Alternar entre Iniciar Sesión y Registrarse
        TextButton(
            onClick = { isRegisterMode = !isRegisterMode }
        ) {
            Text(
                if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión"
                else "¿No tienes cuenta? Regístrate aquí"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Botones de Simulación Social (RF1: Google / Apple)
        Text(
            text = "O ingresa rápidamente con",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                // Simulación directa con Google
                val googleUser = User(
                    id = "google_user",
                    name = "Usuario Google",
                    email = "google.test@upn.pe",
                    role = selectedRole
                )
                onLoginSuccess(googleUser)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google (Simulado)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                // Simulación directa con Apple
                val appleUser = User(
                    id = "apple_user",
                    name = "Usuario Apple",
                    email = "apple.test@upn.pe",
                    role = selectedRole
                )
                onLoginSuccess(appleUser)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Apple (Simulado)")
        }
    }
}