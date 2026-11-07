# Reglas del Asistente y Proyecto: AppMovAdopDeMascotas

Este proyecto es para el curso de Android Jetpack Compose (Universidad Privada del Norte).

## Alcance actual: migración APP_Mascota_Prueva
1. **RF1 — Registro de Usuario:** Registro/login por correo con selección de rol ("Adoptante" vs "Refugio") mediante Appwrite. Google/Apple no están habilitados; no simular sesiones en el flujo real.
2. **RF3 — CRUD de publicaciones de mascotas:** Crear, editar, eliminar y marcar como adoptada, con fotos en Appwrite Storage.
3. **RF4 — Catálogo y detalle de mascota:** Feed con lista de mascotas y pantalla de detalle completa.

## Restricciones y Enfoque Pedagógico
- **Backend real autorizado:** Usar Appwrite para cuentas, perfiles, mascotas, solicitudes, citas, chat y seguimiento. No presentar operaciones fallidas como éxitos simulados.
- **Arquitectura:** Mantener MVVM, la jerarquía en español del origen e inyección manual por constructor mediante un contenedor y una fábrica de ViewModels. Usar estado observable de Compose y StateFlow según el módulo; el estado de sesión debe ser compartido.
- **Git:** Trabajar en `master`; no publicar directamente en `main`.
- **Explicaciones didácticas:** Siempre explicar la ubicación de los archivos y por qué se toman las decisiones de código.

## Skills Disponibles en el Proyecto
- `/guia-codigo`: Guiar paso a paso sin escribir el código directamente por el alumno.
- `/reto-codigo`: Proponer retos pequeños sobre la app para poner a prueba conocimientos.
- `/sustentacion`: Simular la sustentación oral del profesor (preguntas de ubicación, comportamiento y código puntual).
