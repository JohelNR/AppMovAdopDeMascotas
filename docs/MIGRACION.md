# Migración desde APP_Mascota_Prueva

## Procedencia y alcance

- Origen: proyecto del compañero `Nico-LoRRo/App-Moviles-PRUEBAS`, commit `d892b96` ("docs: actualiza documentacion general del proyecto y arquitectura en espanol"), entregado como `APP_Mascota_Prueva.zip` (SHA-256 `9651D6ADB56F84DD8AF92CB651FBA198A86D61F0247424F6286BDD3C7571AF10`).
- Destino: `JohelNR/AppMovAdopDeMascotas`.
- **Criterio: copia exacta.** Todo el código (`app/src`), los recursos, el manifest y la configuración Gradle son idénticos byte a byte al ZIP (solo se normalizaron los finales de línea CRLF → LF). No se añade código propio.
- No se copian: `.git`, `.idea`, `local.properties`, `build/`, `.gradle/` (archivos personales o generados del equipo del compañero).
- Esta sincronización **revierte** la migración anterior (commit `7bbc5a9`, PR #25), que había adaptado el código: inyección con `di/ContenedorApp.kt`, `AplicacionMascotas`, `RecordatoriosTest` y la eliminación de los botones Google/Demo, la siembra de mascotas y las respuestas automáticas del chat. Con esa adaptación, la app ya no se veía ni se comportaba igual que la del compañero.

## Jerarquía de carpetas

Raíz Kotlin: `app/src/main/java/com/example/app_adopcion_d_mascotas/`. Es la misma que la del ZIP:

| Carpeta | Contenido |
|---|---|
| `datos/modelos` | Modelos de datos (`Usuario`, `Mascota`, `SolicitudAdopcion`, `Cita`, `SeguimientoMascota`, `MensajeChat`) |
| `datos/remoto` | `ClienteAppwrite`: endpoint, IDs y servicios de Appwrite |
| `datos/repositorios` | Acceso a Appwrite, un repositorio por servicio |
| `navegacion` | `Rutas.kt` (10 rutas) y `NavegacionApp.kt` (NavHost) |
| `notificaciones` | Canal de notificaciones, alarmas y receptor de recordatorios |
| `ui/componentes` | `FondoApp` (fondo con huellitas) y `TarjetaMascota` |
| `ui/modelovista` | ViewModels de cada módulo |
| `ui/pantallas/<módulo>` | Pantallas Compose |
| `ui/theme` | Colores, tipografía y tema |

## Inyección de dependencias

El proyecto del compañero no usa contenedor, Hilt ni Koin. Las dependencias se resuelven así:

1. `MainActivity.onCreate` llama a `ClienteAppwrite.init(applicationContext)` una sola vez y crea el canal de notificaciones.
2. `ClienteAppwrite` y todos los repositorios son `object` (singletons de Kotlin). Los ViewModels los usan directamente, por ejemplo `RepositorioMascotasAppwrite.getPets()`.
3. Los ViewModels tienen constructor sin parámetros y se obtienen con `viewModel()` de `androidx.lifecycle.viewmodel.compose`.
   - `ModeloCatalogo` y `ModeloSolicitudes` se crean en `NavegacionApp` y se comparten entre pantallas.
   - `ModeloAutenticacion`, `ModeloPerfil`, `ModeloPublicar`, `ModeloSeguimiento` y `ModeloChat` se crean como parámetro por defecto de su pantalla.

**Para añadir un módulo:** crear el repositorio como `object` en `datos/repositorios`, crear el ViewModel sin parámetros en `ui/modelovista` y obtenerlo con `viewModel()` en la pantalla o en `NavegacionApp` si se comparte.

## Manejo de estados

- Los ViewModels de autenticación, catálogo, perfil, publicar, seguimiento y solicitudes exponen estado con `mutableStateOf` (`private set`). La UI lo lee directamente.
- `ModeloChat` usa `MutableStateFlow`/`StateFlow` (mensajes, texto de entrada, carga).
- `RepositorioAutenticacionAppwrite.usuarioActual` es un `MutableStateFlow` con la sesión.
- `RepositorioMascotas` mantiene una lista local `mutableStateListOf` con datos de ejemplo.
- Las operaciones remotas se lanzan en `viewModelScope`.
- Cada clase incluye alias en inglés (`login`, `register`, `typealias AuthViewModel`…) para compatibilidad.

## Módulos

### 1. Autenticación (RF1)

**Archivos:** `datos/modelos/Usuario.kt`, `datos/repositorios/RepositorioAutenticacionAppwrite.kt`, `ui/modelovista/ModeloAutenticacion.kt`, `ui/pantallas/autenticacion/PantallaInicioSesion.kt`.

- Login y registro con correo y contraseña (mínimo 8 caracteres) contra Appwrite Account, con rol Adoptante/Refugio.
- `verificarSesion()` al iniciar.
- **Continuar con Google** (`iniciarSesionConGoogle`): si ya hay sesión, la reutiliza; si no, crea un usuario local simulado ("Usuario Google"). No es OAuth real.
- **Acceso rápido de prueba (Demo)** (`accesoRapidoDemo`): entra como `demo_adoptante` o `demo_refugio` sin pasar por Appwrite.

**Actualizar:** los usuarios Google y Demo no existen en Appwrite, así que las operaciones que dependen de permisos de sesión pueden fallar con ellos.

### 2. Perfil (RF2)

**Archivos:** `ModeloPerfil.kt`, `ui/pantallas/perfil/PantallaPerfil.kt`.

Muestra y edita el avatar, nombre, teléfono y biografía; el avatar se sube con `RepositorioAlmacenamientoAppwrite`. Lista mis mascotas (refugio) o mis solicitudes (adoptante).

### 3. Publicar mascota (RF3)

**Archivos:** `RepositorioMascotasAppwrite.kt`, `RepositorioAlmacenamientoAppwrite.kt`, `ModeloPublicar.kt`, `ui/pantallas/publicar/PantallaCrearEditarMascota.kt`.

Crear, editar y eliminar mascotas con selección de fotos (`seleccionarFotos`, `quitarFoto`) que se suben a Storage.

### 4. Catálogo, urgencias y filtros (RF4–RF6)

**Archivos:** `Mascota.kt`, `ModeloCatalogo.kt`, `ui/pantallas/catalogo/PantallaCatalogo.kt`, `ui/componentes/TarjetaMascota.kt`.

Listado desde Appwrite con búsqueda y filtros por especie, tamaño y sexo (`limpiarFiltros`). Si la colección está vacía, `sembrarMascotasIniciales()` crea mascotas de ejemplo (Max, Luna, …).

### 5. Detalle y mapa (RF4, RF7)

**Archivos:** `ui/pantallas/detalle/PantallaDetalleMascota.kt`, `AndroidManifest.xml`.

Carrusel de fotos, información, acceso a adopción y chat, y un mapa con Google Maps Compose. Las coordenadas (`latitude`, `longitude`) tienen por defecto Lima (-12.0464, -77.0428). El manifest trae la clave `AIzaSyDemoMapsKeyAdopcionMascotas`, que es una clave de ejemplo: para ver el mapa hay que reemplazarla por una clave real.

### 6. Solicitudes de adopción (RF8–RF10)

**Archivos:** `SolicitudAdopcion.kt`, `RepositorioSolicitudesAppwrite.kt`, `ModeloSolicitudes.kt`, `ui/pantallas/solicitudes/`.

Formulario de adopción, listado por rol, y aceptar o rechazar. Al aceptar, la mascota se marca como `ADOPTADA`. El refugio se guarda en la clave remota `shelterNames`.

### 7. Citas y notificaciones (RF14, RF20)

**Archivos:** `Cita.kt`, `ModeloSolicitudes.kt`, `ui/pantallas/citas/PantallaCitas.kt`, `notificaciones/AyudanteNotificaciones.kt`, `notificaciones/AppointmentReminderReceiver.kt`.

Agendar y confirmar citas en Appwrite. El recordatorio local se programa por defecto **10 segundos** después (modo de prueba en vivo). Pide el permiso de notificaciones en Android 13+.

### 8. Chat (RF13)

**Archivos:** `MensajeChat.kt`, `RepositorioChatAppwrite.kt`, `ModeloChat.kt`, `ui/pantallas/chat/PantallaChat.kt`.

Historial ordenado por `timestamp` y suscripción Realtime. Tras enviar un mensaje, a los 2 segundos se genera una **respuesta automática simulada** del refugio (`shelter_host`).

### 9. Seguimiento posadopción (RF11)

**Archivos:** `SeguimientoMascota.kt`, `RepositorioSeguimientoAppwrite.kt`, `ModeloSeguimiento.kt`, `ui/pantallas/seguimiento/PantallaSeguimiento.kt`.

Reportes con notas de salud y adaptación, más fotos, guardados en la colección `seguimientos`.

### 10. Navegación y tema

**Archivos:** `navegacion/Rutas.kt`, `navegacion/NavegacionApp.kt`, `ui/componentes/FondoApp.kt`, `ui/theme/`.

Diez rutas: `login`, `feed`, `detail/{petId}`, `publish`, `adopt/{petId}`, `requests`, `appointments`, `profile`, `chat/{chatId}` y `followup/{petId}`. Usa el fondo marrón oscuro con huellitas.

## Siguientes actualizaciones del compañero

1. Descargar su último commit (o ZIP) y copiar todo excepto `.git`, `.idea`, `local.properties`, `build/` y `.gradle/`.
2. Normalizar los finales de línea a LF y comparar con `diff -rq --strip-trailing-cr` contra el ZIP. No debe haber diferencias fuera de `docs/`.
3. Actualizar este documento con el commit de origen y los módulos que cambiaron.
