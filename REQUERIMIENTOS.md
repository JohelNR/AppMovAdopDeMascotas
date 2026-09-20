# Requerimientos del Proyecto: AppMovAdopDeMascotas

## Issue #1 - RF1 — Registro de Usuario 

Prioridad: Alta · Módulo: Usuarios y Perfiles
**Historia de usuario:** Como usuario nuevo, quiero registrarme e iniciar sesión indicando si soy adoptante o refugio, para acceder a las funciones correspondientes a mi rol.
**Criterios de aceptación:**

1. El formulario de registro permite elegir entre rol "Adoptante" y "Refugio/Publicador".
2. El sistema valida el formato de correo electrónico y la fortaleza de la contraseña.
3. Tras el registro/login exitoso, el usuario es redirigido al Feed Principal.
4. El inicio de sesión social (Google/Apple) crea o reconoce la cuenta correctamente.

<img width="325" height="669" alt="Image" src="https://github.com/user-attachments/assets/5890835b-02e6-4874-b073-20cd4545f1be" />


---

## Issue #2 - RF2 — Gestión del perfil de usuario

Prioridad: Alta · Módulo: Usuarios y Perfiles
Historia de usuario: Como usuario registrado, quiero ver y gestionar mi perfil (publicaciones activas, solicitudes realizadas e historial de adopciones), para tener control sobre mi actividad en la app.
Criterios de aceptación:

1. El perfil de un Adoptante muestra sus solicitudes enviadas y su historial de adopciones.
2. El perfil de un Refugio muestra sus publicaciones activas.
3. Cada elemento del perfil navega al detalle correspondiente al tocarlo.

---

## Issue #3 - RF3 — CRUD de publicaciones de mascotas

Prioridad: Alta · Módulo: Publicaciones y Catálogo
Historia de usuario: Como refugio, quiero crear, editar, eliminar y marcar como adoptada una publicación de mascota, para mantener el catálogo actualizado.
Criterios de aceptación:

1.  El formulario de publicación captura fotos (hasta 5), nombre, edad, raza y descripción.
2. Los cambios (edición/eliminación) se reflejan de inmediato en el feed público.
3. Al marcar una mascota como "Adoptada", deja de ser visible en las búsquedas de adoptantes.

<img width="325" height="889" alt="Image" src="https://github.com/user-attachments/assets/f5b2814f-4df4-4796-b4cc-c05d533e6328" />


---

## Issue #4 - RF4 — Catálogo/feed y detalle de mascota

Prioridad: Alta · Módulo: Publicaciones y Catálogo
**Historia de usuario:** Como adoptante, quiero ver un catálogo general de mascotas y el detalle completo de cada una, para decidir informadamente antes de solicitar una adopción.
**Criterios de aceptación:**

1.  El feed carga de forma paginada/eficiente (sin bloquear la UI).
2.  La pantalla de detalle muestra fotos, nombre, edad, raza, descripción, distancia y refugio de origen.

<img width="250" height="1243" alt="Image" src="https://github.com/user-attachments/assets/7af8c9e7-0766-4033-a329-07fde25f7961" />


---

## Issue #5 - RF5 — Sección de mascotas urgentes

Prioridad: Media · Módulo: Publicaciones y Catálogo
Historia de usuario: Como adoptante, quiero que se destaquen las mascotas urgentes en la pantalla principal, para priorizar los casos que más lo necesitan.
Criterios de aceptación:

1.  Existe un campo/etiqueta "urgente" configurable por el refugio al publicar.
2. Cuando hay publicaciones urgentes, aparece una sección visualmente diferenciada en la parte superior del feed.

---

## Issue #6 - RF6 — Búsqueda y filtros

Prioridad: Alta · Módulo: Búsqueda y Geolocalización
**Historia de usuario:** Como adoptante, quiero buscar y filtrar mascotas por tipo, edad, tamaño, sexo y ubicación, para encontrar rápidamente una mascota compatible con mi estilo de vida.
**Criterios de aceptación:**

1.  Los filtros son combinables entre sí (AND lógico).
2.  Los resultados se actualizan sin recargar toda la app.
3. El contador de resultados coincide con las tarjetas mostradas.

---

## Issue #7 - RF7 — Mapa de ubicación del refugio

Prioridad: Media · Módulo: Búsqueda y Geolocalización
**Historia de usuario:** Como adoptante, quiero ver la ubicación aproximada del refugio en un mapa, para evaluar la distancia y planificar la visita.
**Criterios de aceptación:**

1. El mapa integrado muestra un marcador aproximado (no la dirección exacta) del refugio asociado a la mascota.
2. El servicio de mapas responde correctamente en ambas plataformas (Android/iOS).

<img width="509" height="1600" alt="Image" src="https://github.com/user-attachments/assets/0a25ffd5-d5a5-4b0d-b697-e45317fd5c60" />


---

## Issue #8 - RF8 — Formulario de solicitud de adopción

RF8 — Formulario de solicitud de adopción
Prioridad: Alta · Módulo: Proceso de Adopción
**Historia de usuario:** Como adoptante, quiero enviar una solicitud de adopción mediante un formulario estructurado, para iniciar formalmente el proceso con el refugio.
**Criterios de aceptación:**

1. El formulario exige: motivo de adopción, tipo de vivienda, nivel de experiencia, otras mascotas en el hogar.
2. No permite enviarse si falta un campo obligatorio o si no se acepta el checkbox de Términos y Condiciones.

<img width="325" height="710" alt="Image" src="https://github.com/user-attachments/assets/5ebd89e7-4ae8-48ea-acff-b333c0eb771b" />


---

## Issue #9 - RF9 — Panel de gestión de solicitudes

Prioridad: Alta · Módulo: Proceso de Adopción
**Historia de usuario:** Como refugio, quiero evaluar, aceptar o rechazar las solicitudes recibidas desde un panel dedicado, para gestionar el proceso de adopción de forma ordenada.
**Criterios de aceptación:**

1. El panel lista las solicitudes pendientes con los datos completos del formulario.
2. Permite cambiar el estado (aceptada/rechazada) con un paso de confirmación.

---

## Issue #10 - RF10 — Cambio automático de estado a "Adoptada"

**Historia de usuario:** Como refugio, quiero que el sistema cambie automáticamente el estado de la mascota a "adoptada" al aprobar una solicitud, para evitar solicitudes duplicadas sobre una mascota ya asignada.
**Criterios de aceptación:**

1. Al aceptar una solicitud, la mascota se retira del catálogo público de forma inmediata.
2. Las demás solicitudes pendientes sobre esa mascota se marcan automáticamente como no disponibles.

---

## Issue #11 - RF11 — Formulario de seguimiento posterior a la adopción

Prioridad: Media · Módulo: Proceso de Adopción
**Historia de usuario:** Como refugio, quiero enviar y recibir formularios de seguimiento posterior a la adopción, para verificar el bienestar de la mascota en su nuevo hogar.
**Criterios de aceptación:**

1. El adoptante recibe un recordatorio para completar el formulario en una fecha posterior a la adopción confirmada.
2. Las respuestas quedan asociadas al historial de esa adopción.

---

## Issue #12 - RF12 — Favoritos

Prioridad: Media · Módulo: Interacción y Utilidades
**Historia de usuario:** Como adoptante, quiero guardar mascotas como favoritas, para volver a consultarlas fácilmente más adelante.
**Criterios de aceptación:**

1. El ícono de favorito cambia de estado (lleno/vacío) al tocarlo.
2. La mascota aparece en la sección "Favoritos" dentro de Actividad.

---

## Issue #13 - RF13 — Chat adoptante–refugio

Prioridad: Media · Módulo: Interacción y Utilidades
**Historia de usuario:** Como adoptante o refugio, quiero comunicarme por chat una vez iniciado el interés por una mascota, para resolver dudas antes o durante el proceso de adopción.
**Criterios de aceptación:**

1. El chat se habilita automáticamente al enviar una solicitud de adopción.
2. Permite el envío de texto e imágenes entre ambas partes.

---

## Issue #14 - RF14 — Notificaciones

Prioridad: Media · Módulo: Interacción y Utilidades
**Historia de usuario:** Como usuario, quiero recibir notificaciones sobre cambios en mis solicitudes y nuevos mensajes, para no perder actualizaciones importantes.
**Criterios de aceptación:**

1. Las notificaciones push se disparan ante cambios de estado de solicitud y mensajes nuevos.
2. Son configurables (activar/desactivar) desde el perfil.

---

## Issue #15 - RF15 — Compartir en redes sociales

Prioridad: Media · Módulo: Interacción y Utilidades
Historia de usuario: Como adoptante, quiero compartir la ficha de una mascota en redes sociales, para ayudar a difundir su adopción entre mis contactos.
Criterios de aceptación:

1. Un botón de compartir genera un enlace o imagen resumen de la mascota.
2. Es compatible con las principales redes sociales (WhatsApp, Instagram, Facebook).

---

## Issue #16 - RF16 — Modo oscuro

Prioridad: Media · Módulo: Interacción y Utilidades
**Historia de usuario:** Como usuario, quiero alternar la interfaz entre modo claro y modo oscuro, para adaptar la app a mis preferencias visuales y ahorrar batería.
**Criterios de aceptación:**

1. El cambio se aplica de inmediato a toda la app, sin reiniciarla.
2. La preferencia se conserva entre sesiones.

---

## Issue #17 - RF17 — Panel de estadísticas del refugio

Prioridad: Media · Módulo: Interacción y Utilidades
**Historia de usuario:** Como refugio, quiero ver un panel de estadísticas con las adopciones logradas por mes, para medir el impacto de mi gestión.
**Criterios de aceptación:**

1. El panel muestra un gráfico o resumen numérico de adopciones concretadas por período.
2. Es filtrable por rango de fechas.

---

## Issue #18 - diseño del flujo de navegación y la creación de prototipos visuales de alta fidelidad.

[Investigacion.docx](https://github.com/user-attachments/files/31642158/Investigacion.docx)

---

## Issue #19 - RF18 — Restablecimiento de contraseña

Prioridad: Alta · Módulo: Usuarios y Perfiles
**Historia de usuario:** Como usuario que olvidó su contraseña, quiero solicitar un enlace o código de
verificación a mi correo electrónico registrado, para poder restablecerla y recuperar el acceso a mi cuenta.
**Criterios de aceptación:**

1. El usuario puede solicitar la recuperación ingresando su correo electrónico registrado.
2. El sistema envía un enlace o código de verificación con validez limitada (p. ej. 15-30 minutos).
3. El sistema exige una nueva contraseña que cumpla los requisitos mínimos de seguridad antes de
guardarla.
4. Tras el restablecimiento exitoso, el usuario puede iniciar sesión con la nueva contraseña y el
enlace/código anterior deja de ser válido.

---

## Issue #20 - RF19— Verificación de identidad de refugios

Prioridad: Media · Módulo: Usuarios y Perfiles
**Historia de usuario:** Como refugio/publicador, quiero cargar mis documentos de acreditación para
verificar mi identidad, para generar confianza en los adoptantes y habilitar mi cuenta como publicador
validado.
**Criterios de aceptación:**

1. El sistema permite adjuntar uno o más documentos (PDF/imagen) durante o después del registro del rol
"Refugio/Publicador".
2. El sistema valida el formato y el peso máximo del archivo antes de aceptarlo.
3. La cuenta queda en estado "Pendiente de verificación" hasta que un administrador revise los
documentos cargados.
4. El refugio recibe una notificación cuando su cuenta es aprobada o rechazada, indicando el motivo en
caso de rechazo.

---

## Issue #21 - RF20 — Coordinación de citas y entrevistas

Prioridad: Alta · Módulo: Proceso de Adopción, Citas y Seguimiento
**Historia de usuario:** Como refugio, quiero coordinar, agendar y confirmar una cita o entrevista con el
adoptante tras la preaprobación de su solicitud, para conocerlo antes de concretar la adopción.
**Criterios de aceptación:**

1. El refugio puede proponer una o más fechas/horarios disponibles al adoptante.
2. El adoptante puede aceptar, rechazar o proponer un nuevo horario alternativo.
3. La cita confirmada queda visible para ambas partes con fecha, hora y modalidad (presencial o virtual).
4. Ambos usuarios reciben una notificación de recordatorio antes de la cita agendada.

---

## Issue #22 - RF21 — Reporte y denuncia de publicaciones

Prioridad: Media · Módulo: Interacción, Social y Moderación
**Historia de usuario:** Como usuario, quiero reportar o denunciar una publicación sospechosa, inapropiada
o fraudulenta, para que el equipo administrativo la revise y tome acción.
**Criterios de aceptación:**

1. Cada publicación tiene una opción visible para reportarla, con un motivo obligatorio (fraude, contenido
 inapropiado, información falsa, u otro).
2. El reporte queda registrado con la fecha, el usuario que reporta y la publicación afectada.
3. Existe un panel administrativo para revisar los reportes y tomar acciones (advertir al publicador, ocultar
o eliminar la publicación).
4. El usuario que reportó recibe una confirmación de que su reporte fue recibido.

---

## Issue #23 - RF22 — Valoraciones y comentarios sobre el refugio

Prioridad: Media · Módulo: Interacción, Social y Moderación
**Historia de usuario:** Como adoptante, quiero dejar una valoración y un comentario sobre la atención del
refugio una vez finalizado el proceso de adopción, para ayudar a otros adoptantes y reconocer la gestión
del refugio.

**Criterios de aceptación:**

1. La opción de valorar/comentar solo se habilita después de que la adopción quedó marcada como
concretada.
2. La valoración incluye una calificación numérica (por ejemplo, de 1 a 5 estrellas) y un comentario de texto
opcional.
3. El perfil del refugio muestra el promedio de calificaciones y la lista de comentarios recibidos.
4. El refugio puede visualizar, pero no eliminar, las valoraciones recibidas, para mantener la transparencia
del sistema.

---

## Issue #24 - diseño del flujo de navegación y la creación de prototipos visuales de alta fidelidad.

[Investigacion.docx](https://github.com/user-attachments/files/31767307/Investigacion.docx)


---

