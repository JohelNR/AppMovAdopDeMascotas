package com.example.app_adopcion_d_mascotas.ui.pantallas.autenticacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_adopcion_d_mascotas.datos.modelos.RolUsuario
import com.example.app_adopcion_d_mascotas.datos.modelos.Usuario
import com.example.app_adopcion_d_mascotas.ui.modelovista.ModeloAutenticacion

/*
 * ============================================================================
 * ARCHIVO: PantallaInicioSesion.kt
 * CAPA: UI -> Pantallas -> Autenticación (RF1: Registro e Inicio de Sesión)
 * ¿QUÉ HACE?: Dibuja la pantalla de bienvenida donde el usuario puede:
 *             1. Iniciar sesión con su correo y contraseña reales en Appwrite.
 *             2. Registrarse eligiendo claramente su rol: ADOPTANTE o REFUGIO.
 * ============================================================================
 */
@Composable
fun PantallaInicioSesion(
    modeloAutenticacion: ModeloAutenticacion = viewModel(),
    alIniciarSesion: (Usuario) -> Unit
) {
    // Paso 1: Variables de estado en español para guardar lo que escribe el usuario
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.ADOPTANTE) }
    var modoRegistro by remember { mutableStateOf(false) }
    var mostrarContrasena by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Paso 2: Logo circular y título principal de la aplicación
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Logo AdoptaAmor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "AdoptaAmor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (modoRegistro)
                        "Crea tu cuenta oficial en la nube"
                    else
                        "Conectando corazones con huellitas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selector tipo pestaña superior: "Iniciar Sesión" vs "Registrarse"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !modoRegistro,
                        onClick = {
                            modoRegistro = false
                            modeloAutenticacion.limpiarError()
                        },
                        label = {
                            Text(
                                text = "Iniciar Sesión",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = modoRegistro,
                        onClick = {
                            modoRegistro = true
                            modeloAutenticacion.limpiarError()
                        },
                        label = {
                            Text(
                                text = "Crear Cuenta",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Paso 3: Si está en modo Registro, mostrar tarjetas grandes de Rol y campo de Nombre
                AnimatedVisibility(visible = modoRegistro) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "1. ¿Cómo deseas registrarte? *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TarjetaSelectorRol(
                                titulo = "Adoptante",
                                subtitulo = "Quiero adoptar una mascota",
                                icono = Icons.Default.Person,
                                seleccionado = rolSeleccionado == RolUsuario.ADOPTANTE,
                                alSeleccionar = { rolSeleccionado = RolUsuario.ADOPTANTE },
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaSelectorRol(
                                titulo = "Refugio",
                                subtitulo = "Publico mascotas en adopción",
                                icono = Icons.Default.Home,
                                seleccionado = rolSeleccionado == RolUsuario.REFUGIO,
                                alSeleccionar = { rolSeleccionado = RolUsuario.REFUGIO },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                modeloAutenticacion.limpiarError()
                            },
                            label = {
                                Text(
                                    if (rolSeleccionado == RolUsuario.REFUGIO)
                                        "Nombre del Refugio o Albergue *"
                                    else
                                        "Tu Nombre Completo *"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (rolSeleccionado == RolUsuario.REFUGIO) Icons.Default.Home else Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Paso 4: Campo de Correo Electrónico
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        modeloAutenticacion.limpiarError()
                    },
                    label = { Text("Correo electrónico *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Paso 5: Campo de Contraseña con botón para mostrar/ocultar texto
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        modeloAutenticacion.limpiarError()
                    },
                    label = { Text("Contraseña (mínimo 8 caracteres) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                imageVector = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar u ocultar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Paso 6: Mostrar recuadro de error si alguna validación falla
                modeloAutenticacion.mensajeError?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Paso 7: Botón principal para enviar a Appwrite Cloud
                Button(
                    onClick = {
                        if (modoRegistro) {
                            modeloAutenticacion.registrarse(
                                nombre = nombre,
                                correo = correo,
                                contrasena = contrasena,
                                rol = rolSeleccionado,
                                alTenerExito = alIniciarSesion
                            )
                        } else {
                            modeloAutenticacion.iniciarSesion(
                                correo = correo,
                                contrasena = contrasena,
                                alTenerExito = alIniciarSesion
                            )
                        }
                    },
                    enabled = !modeloAutenticacion.estaCargando,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (modeloAutenticacion.estaCargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Conectando con Appwrite...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = if (modoRegistro)
                                "Registrarme como ${if (rolSeleccionado == RolUsuario.REFUGIO) "Refugio" else "Adoptante"}"
                            else
                                "Ingresar a mi Cuenta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Enlace inferior para cambiar entre Registro e Inicio de Sesión
                TextButton(
                    onClick = {
                        modoRegistro = !modoRegistro
                        modeloAutenticacion.limpiarError()
                    }
                ) {
                    Text(
                        text = if (modoRegistro)
                            "¿Ya tienes una cuenta registrada? Inicia sesión aquí"
                        else
                            "¿Eres nuevo adoptante o refugio? Regístrate aquí"
                    )
                }
            }
        }
    }
}

/*
 * Componente visual para elegir claramente entre el rol ADOPTANTE y el rol REFUGIO
 */
@Composable
private fun TarjetaSelectorRol(
    titulo: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    alSeleccionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorFondo = if (seleccionado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val colorBorde = if (seleccionado) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = modifier
            .clickable { alSeleccionar() },
        shape = RoundedCornerShape(14.dp),
        color = colorFondo,
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, colorBorde)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitulo,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
