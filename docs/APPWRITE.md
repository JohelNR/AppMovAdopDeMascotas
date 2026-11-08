# Configuración y Arquitectura de Appwrite Cloud (100% en Español)

El módulo remoto [`datos/remoto/ClienteAppwrite.kt`](../app/src/main/java/com/example/app_adopcion_d_mascotas/datos/remoto/ClienteAppwrite.kt) conecta toda la aplicación Android en tiempo real con **Appwrite Cloud (Región Frankfurt)** utilizando identificadores y esquemas **100% en español**:

| Recurso | Identificador en Appwrite Cloud | Descripción |
|---|---|---|
| **Endpoint** | `https://fra.cloud.appwrite.io/v1` | Servidor en la nube de Appwrite |
| **Proyecto** | `6aceca44001361ba94f0` | Proyecto principal del curso |
| **Base de Datos** | `adopcion_db` | Base de datos relacional/documental |
| **Bucket Storage** | `fotos_mascotas` | Almacenamiento de imágenes recortadas y optimizadas |
| **Tabla 1: Usuarios** | `usuarios` | Perfiles de Adoptantes y Refugios (RF1, RF2) |
| **Tabla 2: Mascotas** | `mascotas` | Catálogo de mascotas, fotos y coordenadas GPS (RF3, RF4, RF5, RF6, RF7) |
| **Tabla 3: Solicitudes** | `solicitudes` | Postulaciones de adopción y evaluación en cascada (RF8, RF9, RF10) |
| **Tabla 4: Citas** | `citas` | Agenda de entrevistas y entregas presenciales/virtuales (RF20) |
| **Tabla 5: Seguimientos** | `seguimientos` | Reportes post-adopción con fotos de evidencia (RF11) |
| **Tabla 6: Mensajes** | `mensajes` | Chat en tiempo real Adoptante ↔ Refugio con WebSockets (RF13) |

---

## Esquema de Tablas y Columnas (100% en Español)

Todas las tablas cuentan con permisos configurados para usuarios autenticados y lectura de catálogo, garantizando **cero listas falsas en memoria RAM del teléfono**.

| Tabla (`tableId`) | Columnas en Español (`key` : `tipo`) | Índices Configurados |
|---|---|---|
| **`usuarios`** | `idUsuario` (string), `nombre` (string), `correo` (string), `rol` (string), `telefono` (string), `biografia` (string), `fotoPerfilId` (string) | `idx_idUsuario` (`idUsuario` ASC) |
| **`mascotas`** | `nombre` (string), `especie` (string), `raza` (string), `edadAnios` (integer), `sexo` (string), `tamano` (string), `descripcion` (string), `nombreRefugio` (string), `idRefugio` (string), `ubicacion` (string), `latitud` (double), `longitud` (double), `esUrgente` (boolean), `estado` (string), `fotos` (string[]) | Ordenamiento nativo por `$createdAt` DESC |
| **`solicitudes`** | `idMascota` (string), `nombreMascota` (string), `idSolicitante` (string), `nombreSolicitante` (string), `correoSolicitante` (string), `nombreRefugio` (string), `motivo` (string), `tienePatio` (boolean), `tieneExperiencia` (boolean), `aceptaTerminos` (boolean), `estado` (string) | `idx_idSolicitante` (`idSolicitante` ASC), `idx_mascota_estado` (`idMascota`, `estado`) |
| **`citas`** | `idSolicitud` (string), `nombreMascota` (string), `nombreSolicitante` (string), `nombreRefugio` (string), `fecha` (string), `hora` (string), `modalidad` (string), `estado` (string) | Ordenamiento nativo por `$createdAt` DESC |
| **`seguimientos`** | `idMascota` (string), `nombreMascota` (string), `idSolicitante` (string), `nombreSolicitante` (string), `nombreRefugio` (string), `fecha` (string), `notasSalud` (string), `notasAdaptacion` (string), `fotos` (string[]), `estado` (string) | `idx_idMascota` (`idMascota` ASC) |
| **`mensajes`** | `idChat` (string), `nombreMascota` (string), `idEmisor` (string), `nombreEmisor` (string), `idReceptor` (string), `texto` (string), `fechaHora` (integer 64-bit), `leido` (boolean) | `idx_idChat_fecha` (`idChat` ASC, `fechaHora` ASC) |

---

## Estados del Dominio en Español

- **RolUsuario (`rol`)**: `ADOPTANTE`, `REFUGIO`
- **EstadoMascota (`estado`)**: `DISPONIBLE`, `EN_PROCESO`, `ADOPTADA`
- **EstadoSolicitud (`estado`)**: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`, `NO_DISPONIBLE`
- **EstadoCita (`estado`)**: `PROGRAMADA`, `CONFIRMADA`, `REPROGRAMAR`, `COMPLETADA`, `CANCELADA`
- **Seguimiento (`estado`)**: `ENVIADO`, `REVISADO`

---

## Mapa Interactivo y GPS Real (RF7)

El componente [`VisorMapaInteractivo.kt`](../app/src/main/java/com/example/app_adopcion_d_mascotas/ui/componentes/VisorMapaInteractivo.kt) renderiza cartografía callejera real mediante **OpenStreetMap + Leaflet** sin requerir tarjeta de crédito ni claves de facturación restringidas, permitiendo:
1. Visualizar en tiempo real el marcador interactivo del refugio y su distancia en kilómetros mediante la fórmula geodésica de **Haversine**.
2. Seleccionar la ubicación exacta en el mapa o mediante el **GPS del teléfono** (`FusedLocationProviderClient`) al publicar o editar una mascota.
3. Abrir rutas de navegación giro a giro directamente en la aplicación oficial de **Google Maps**.
