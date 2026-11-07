# Migración desde APP_Mascota_Prueva

## Procedencia y alcance

- Destino: `JohelNR/AppMovAdopDeMascotas`, rama `master`.
- Base: `96c2a765e2b7ca2b67abfe5296a9e8057ae22759` (`main`).
- Origen: ZIP entregado por el usuario; SHA-256 `9651D6ADB56F84DD8AF92CB651FBA198A86D61F0247424F6286BDD3C7571AF10`.
- Se migran archivos fuente, navegación y manifest. No se importan el `.git`, builds, cachés, configuración de IDE ni `local.properties` del compañero.
- Appwrite real fue autorizado expresamente; `AGENTS.md` refleja el nuevo alcance.
- El alcance es migrar los módulos presentes en el ZIP. `REQUERIMIENTOS.md` es el backlog completo: OAuth, chat con imágenes, favoritos, push remoto e iOS no quedan implementados por esta migración.

## Jerarquía y equivalencias

Raíz Kotlin: `app/src/main/java/com/example/app_adopcion_d_mascotas/`.

| Directorio anterior del destino | Directorio integrado, igual al ZIP |
|---|---|
| `data/model` | `datos/modelos` |
| `data/remote` | `datos/remoto` |
| `data/repository` | `datos/repositorios` |
| `navigation` | `navegacion` |
| `ui/components` | `ui/componentes` |
| `ui/viewmodel` | `ui/modelovista` |
| `ui/screens` | `ui/pantallas` |
| `ui/theme` | `ui/theme` |
| Nuevos | `notificaciones`, `di` |

Se reemplazan las implementaciones anteriores equivalentes para evitar dos grafos de navegación o repositorios de sesión diferentes. El ZIP proporciona aliases de tipos y wrappers en inglés dentro de los nuevos paquetes. Estos no conservan los imports a los paquetes antiguos.

## Inyección de dependencias

No había Hilt, Koin ni una fábrica definida en los archivos entregados. Se implementa inyección manual:

1. Android crea `AplicacionMascotas`, registrada en el manifest.
2. `ClienteAppwrite.init` usa el contexto de Application una sola vez.
3. `di/ContenedorApp.kt` construye un repositorio por servicio. Solicitudes recibe también el repositorio de mascotas.
4. La fábrica recibe la clase del ViewModel e inyecta sus repositorios por constructor.
5. `modeloVista()` utiliza esa fábrica y el `ViewModelStoreOwner` de Compose. Las pantallas importan esta función como `viewModel`.

Los ViewModels de catálogo, autenticación y solicitudes se crean en la raíz de navegación; publicar, perfil, chat y seguimiento se vinculan a la entrada de navegación. Rotar la Activity no crea otro cliente ni otra sesión. Al salir de una conversación, su ViewModel cierra Realtime.

Para añadir un módulo: crear su repositorio en `datos/repositorios`, registrarlo en el contenedor, recibirlo por constructor en el ViewModel y añadir el caso a la fábrica. No construir repositorios dentro de composables.

## Estado compartido

- La sesión autoritativa reside en `RepositorioAutenticacionAppwrite.usuarioActual` (`StateFlow`). Navegación y chat la observan con `collectAsStateWithLifecycle`.
- El arranque verifica la sesión antes de mostrar el grafo. Login, registro, actualización de perfil y logout utilizan el mismo repositorio.
- Los formularios y listas de los demás módulos mantienen `mutableStateOf`, como en el origen. Las pantallas reciben datos y callbacks; no mantienen otra base de datos.
- Las operaciones remotas usan `viewModelScope`; carga, envío y mensajes de resultado se muestran en la UI. Chat utiliza flujos de solo lectura y conserva el texto si falla el envío.
- Logout limpia solicitudes y citas y cancela sus trabajos pendientes antes de reutilizar el flujo con otra cuenta.

## Módulos

### 1. Autenticación — RF1

**Archivos:** `datos/modelos/Usuario.kt`, `datos/repositorios/RepositorioAutenticacionAppwrite.kt`, `ui/modelovista/ModeloAutenticacion.kt`, `ui/pantallas/autenticacion/PantallaInicioSesion.kt`.

Registro y login por correo contra Account de Appwrite, con perfil/rol en la colección de usuarios. El registro actualiza la sesión compartida. Se verifica la sesión en el arranque y se elimina la sesión remota al salir. Se retiran los botones de cuentas inventadas y Google simulado; no equivalían a OAuth real.

**Actualizar:** conservar las claves `userId` y `role` del perfil. Probar registro, credenciales incorrectas, reabrir la app y cambiar de cuenta.

### 2. Perfil — RF2

**Archivos:** `ModeloPerfil.kt`, `ui/pantallas/perfil/PantallaPerfil.kt`, repositorios de autenticación, almacenamiento, mascotas y solicitudes.

Incluye avatar, nombre, teléfono, biografía y contenido por rol. El teléfono opcional admite nueve dígitos. Una subida fallida del avatar muestra error; una actualización exitosa propaga el usuario a navegación y chat.

**Actualizar:** añadir atributos nuevos tanto al modelo como al mapeo Appwrite. La asociación heredada de mascotas/citas con refugios se basa en el nombre; renombrar un refugio requiere actualizar también esas referencias en el backend.

### 3. Publicación y almacenamiento — RF3

**Archivos:** `RepositorioMascotasAppwrite.kt`, `RepositorioAlmacenamientoAppwrite.kt`, `ModeloPublicar.kt`, `ui/pantallas/publicar/PantallaCrearEditarMascota.kt`.

CRUD remoto, selector de fotos y almacenamiento de IDs de archivos. Conserva fotos anteriores al editar y limita el total a cinco. El formulario valida edad y coordenadas. Se interrumpe el guardado si falla una foto, en vez de informar éxito con imágenes faltantes.

**Actualizar:** mantener sincronizados los campos de creación, edición y lectura. Los archivos subidos antes de un fallo posterior pueden quedar sin referencia; la eliminación de publicaciones no elimina automáticamente los archivos del bucket.

### 4. Catálogo, urgencias y filtros — RF4–RF6

**Archivos:** `datos/modelos/Mascota.kt`, `ModeloCatalogo.kt`, `ui/pantallas/catalogo/PantallaCatalogo.kt`, `ui/componentes/TarjetaMascota.kt`.

Listado remoto con filtros combinables de texto, especie, tamaño y sexo; se excluyen las mascotas adoptadas. Se mantienen tarjetas y estilo Material 3 del ZIP. El catálogo muestra error y reintento cuando falla la lectura. Se elimina la siembra automática de mascotas de ejemplo al abrir un catálogo vacío.

**Actualizar:** los filtros se calculan sobre la lista del ViewModel, sin duplicar estado. Los aliases en inglés conservan compatibilidad con los componentes migrados.

### 5. Detalle y mapas — RF4, RF7

**Archivos:** `ui/pantallas/detalle/PantallaDetalleMascota.kt`, modelo/repositorio de mascotas, `app/build.gradle.kts`, manifest.

Carrusel, información, acciones de publicación/adopción, Google Maps y apertura de aplicación externa de mapas. Las coordenadas se guardan y recuperan de Appwrite. Los documentos antiguos sin coordenadas usan el valor heredado de Lima hasta ser editados.

**Actualizar:** configurar `MAPS_API_KEY` como propiedad Gradle y habilitar Maps SDK for Android. Si falta la clave se muestra un aviso, no un mapa falsamente configurado. No se solicita la ubicación del dispositivo.

### 6. Solicitudes — RF8–RF10

**Archivos:** `SolicitudAdopcion.kt`, `RepositorioSolicitudesAppwrite.kt`, `ModeloSolicitudes.kt`, `ui/pantallas/solicitudes/`.

Formulario con consentimiento, listado por rol, aceptar/rechazar y cierre de otras postulaciones. Se mantiene la clave remota heredada `shelterNames`. El refugio ya no recibe todas las solicitudes cuando no hay coincidencias; se exige coincidencia exacta de nombre. Se añade acceso al chat desde la solicitud para ambos participantes.

**Actualizar:** aceptar una solicitud encadena varias escrituras remotas; no es una transacción atómica. El control definitivo de permisos y de aceptación concurrente debe residir en Appwrite. La app comprueba el resultado de marcar la mascota adoptada.

### 7. Citas y notificaciones — RF14, RF20

**Archivos:** `Cita.kt`, `ModeloSolicitudes.kt`, `ui/pantallas/citas/PantallaCitas.kt`, `notificaciones/AyudanteNotificaciones.kt`, `AppointmentReminderReceiver.kt`.

Citas en Appwrite, confirmación, avisos locales y alarmas. Se reemplaza el recordatorio demo de diez segundos por la fecha/hora ingresada (`dd/MM/aaaa HH:mm`), validada estrictamente y convertida usando la zona horaria del dispositivo.

**Actualizar:** probar permiso de notificaciones en Android 13+ y permiso de alarmas exactas; sin este último se usa una alarma inexacta. Los recordatorios son locales al dispositivo que agenda y no se reprograman tras reiniciar el teléfono.

### 8. Chat — RF13

**Archivos:** `MensajeChat.kt`, `RepositorioChatAppwrite.kt`, `ModeloChat.kt`, `ui/pantallas/chat/PantallaChat.kt`.

Persistencia real, historial paginado y suscripción Realtime. Se usa una sola clave de conversación `petId_applicantId` desde detalle, perfil y solicitudes. El destinatario se resuelve en perfiles; un nombre inexistente o ambiguo produce error. Los mensajes reciben permisos de lectura para remitente y destinatario.

Se eliminan respuestas automáticas y mensajes ficticios. El ID enviado es el ID del documento; historial, respuesta y evento Realtime se combinan por ID para evitar duplicados. La burbuja se identifica por el usuario real de la sesión.

**Actualizar:** conservar el esquema de mensajes y la identidad de la conversación. Migraciones futuras pueden reemplazar la búsqueda de destinatario por nombre por referencias de usuario inmutables en todas las entidades. Probar con dos cuentas reales y permisos de documentos habilitados.

### 9. Seguimiento posadopción — RF11

**Archivos:** `SeguimientoMascota.kt`, `RepositorioSeguimientoAppwrite.kt`, `ModeloSeguimiento.kt`, `ui/pantallas/seguimiento/PantallaSeguimiento.kt`.

Historial y reportes con notas de salud/adaptación y hasta tres fotos. Se conserva `applicantId` al leer y se crea el reporte como `ENVIADO`, no `REVISADO`. Fallos de nube/subida se muestran como errores; no se guardan en una lista local presentada como persistencia remota.

**Actualizar:** mantener `petId` como vínculo y configurar permisos para adoptante/refugio según el proyecto de Appwrite.

### 10. Navegación y tema

**Archivos:** `navegacion/Rutas.kt`, `NavegacionApp.kt`, `ui/componentes/FondoApp.kt`, `ui/theme/`.

Se migran las diez rutas del ZIP y los componentes de apariencia. El package/applicationId original se conserva. El tema no cambia de carpeta. Se mantiene el toolchain del destino y se agregan únicamente las dependencias de Maps y lifecycle-runtime-compose; `minSdk` pasa a 26 como en el ZIP.

## Siguientes actualizaciones del compañero

Comparar los mismos directorios del ZIP con esta rama. Revisar especialmente los archivos modificados durante la integración: contenedor, sesión, chat, manejo de errores, coordenadas y recordatorios. No sustituirlos ciegamente por sus versiones demo. Ejecutar el recorrido de [VALIDACION.md](VALIDACION.md) y mantener la documentación de esquema junto con cada cambio.
