# Reglas del Asistente y Proyecto: AppMovAdopDeMascotas

Este proyecto es para el curso de Android Jetpack Compose (Universidad Privada del Norte).

## Alcance Primera Entrega
1. **RF1 — Registro de Usuario:** Formulario de registro/login con selección de rol ("Adoptante" vs "Refugio"). El login con Google/Apple es una **SIMULACIÓN**.
2. **RF3 — CRUD de publicaciones de mascotas:** Crear, editar, eliminar y marcar como adoptada. Las fotos son una **SIMULACIÓN** (usar recursos locales o placeholders en memoria, sin nube).
3. **RF4 — Catálogo y detalle de mascota:** Feed con lista de mascotas y pantalla de detalle completa.

## Restricciones y Enfoque Pedagógico
- **Sin Firebase ni backend real en esta entrega:** Todo se gestiona en memoria local simulada.
- **Sin ViewModels complejos ni inyección de dependencias pesadas:** Salvo que el usuario lo solicite expresamente, priorizar el manejo de estado con herramientas nativas de Jetpack Compose (`remember`, `mutableStateOf`, `mutableStateListOf`, State Hoisting). Esto garantiza que el estudiante comprenda cada línea y pueda sustentar con fluidez.
- **Explicaciones didácticas:** Siempre explicar la ubicación de los archivos y por qué se toman las decisiones de código.

## Skills Disponibles en el Proyecto
- `/guia-codigo`: Guiar paso a paso sin escribir el código directamente por el alumno.
- `/reto-codigo`: Proponer retos pequeños sobre la app para poner a prueba conocimientos.
- `/sustentacion`: Simular la sustentación oral del profesor (preguntas de ubicación, comportamiento y código puntual).
