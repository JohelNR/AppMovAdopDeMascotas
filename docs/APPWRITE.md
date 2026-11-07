# Configuración de Appwrite

`datos/remoto/ClienteAppwrite.kt` conserva el endpoint y los identificadores que ya tenía el repositorio destino:

| Recurso | Identificador |
|---|---|
| Endpoint | `https://fra.cloud.appwrite.io/v1` |
| Proyecto | `6aceca44001361ba94f0` |
| Base de datos | `adopcion_db` |
| Bucket | `fotos_mascotas` |
| Mascotas | `mascotas` |
| Perfiles | `6acf7f910021e8558b65` |
| Solicitudes | `6acf8155000ecaf2f811` |
| Citas | `6acf82b100251fdf4d9b` |
| Seguimiento, nuevo | `seguimientos` |
| Mensajes, nuevo | `mensajes` |

No se ha modificado la consola remota. Antes de probar los módulos nuevos, crear/verificar estos recursos con la cuenta administradora del proyecto. El APK utiliza sesiones de usuario, no una API key de servidor.

## Plataforma y servicios

Registrar la plataforma Android `com.example.app_adopcion_d_mascotas`. Activar registro y autenticación email/password. El bucket debe aceptar imágenes y permitir su lectura por los usuarios que las visualizan; las URLs de fotos se construyen con el endpoint del bucket. Configurar límites de tamaño compatibles con las fotos elegidas.

## Atributos

`string[]` significa atributo de texto con `array=true`. Las longitudes máximas deben admitir el contenido del formulario (por ejemplo, 4096 para notas/descripción/texto; 255 para nombres; 64 para IDs; 2048 si se admiten URLs de imágenes).

| Colección | Campos usados por el cliente |
|---|---|
| Perfiles | `userId`, `name`, `email`, `role`, `phone`, `bio`, `avatarPhotoId`: string |
| Mascotas | `name`, `breed`, `species`, `description`, `shelterName`, `status`, `gender`, `size`, `location`: string; `ageYears`: integer; `isUrgent`: boolean; `photos`: string[]; **`latitude`, `longitude`: float** |
| Solicitudes | `petId`, `petName`, `applicantId`, `applicantName`, `applicantEmail`, **`shelterNames`**, `reason`, `status`: string; `hasYard`, `hasExperience`, `termsAccepted`: boolean |
| Citas | `requestId`, `petName`, `applicantName`, `shelterName`, `date`, `time`, `modality`, `status`: string |
| Seguimientos | `petId`, `petName`, `applicantId`, `applicantName`, `shelterName`, `date`, `healthNotes`, `adaptationNotes`, `status`: string; `photos`: string[] |
| Mensajes | `chatId`, `petName`, `senderId`, `senderName`, `receiverId`, `text`: string; `timestamp`: integer de 64 bits (milisegundos); `isRead`: boolean |

Añadir las coordenadas como opcionales para conservar documentos antiguos; el formulario nuevo exige coordenadas válidas. `petName` de mensajes puede estar vacío al abrir desde perfil. `phone`, `bio` y `avatarPhotoId` también pueden estar vacíos.

### Valores de estado

- Roles: `ADOPTANTE`, `REFUGIO`.
- Mascota: `DISPONIBLE`, `ADOPTADA`.
- Solicitud: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`, `NO_DISPONIBLE`.
- Citas: consultar `datos/modelos/Cita.kt` para los valores del enum.
- Seguimiento nuevo: `ENVIADO`.

## Índices y permisos

Crear índices para las consultas: perfiles por `userId` y `name`; solicitudes por `applicantId` y por `petId`/`status`; seguimientos por `petId`; mensajes por `chatId`/`timestamp` ascendente. Las consultas también ordenan por `$createdAt`.

Los permisos del servidor deben permitir leer/escribir los documentos correspondientes a cada usuario y rol. Los filtros de la pantalla no sustituyen estos permisos. Conservar las reglas del proyecto existente y ampliar las colecciones nuevas:

- Mensajes: creación para usuarios autenticados y seguridad por documento. El cliente asigna lectura a los dos participantes. No añadir lectura pública a la colección, porque ampliaría el acceso concedido por los documentos. Realtime respeta esos permisos.
- Seguimientos: lectura para el adoptante/refugio correspondientes y creación para el adoptante autorizado.
- Perfiles: la búsqueda del interlocutor necesita acceso de lectura a su perfil. El uso heredado de nombres exige nombres de interlocutor únicos para resolver el chat.
- Mascotas, solicitudes, citas y archivos: aplicar permisos de propietario/refugio según las reglas existentes. El `role` escrito en un perfil no configura automáticamente roles o Teams de Appwrite.

La migración del cliente no crea reglas remotas, índices ni Functions. Si falta un atributo, una colección o un permiso, corregirlo en la consola y volver a probar; no habilitar simulaciones como sustitución.

## Google Maps

Crear una clave con Maps SDK for Android habilitado y restricción al package y certificado de firma correspondientes. Definirla en `~/.gradle/gradle.properties`:

```properties
MAPS_API_KEY=tu_clave
```

También puede pasarse con `-PMAPS_API_KEY=...`. El manifest usa un placeholder; no contiene la clave ficticia del ZIP. Sin clave, el resto de la aplicación se puede compilar y ejecutar.
