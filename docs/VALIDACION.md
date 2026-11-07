# Validación de la migración

## Comprobaciones automatizadas

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
git diff --check
```

`RecordatoriosTest` cubre fechas imposibles, años bisiestos, horas inválidas y conversión a la zona horaria local. Las pruebas de compilación/lint no comprueban permisos ni contenido del servidor Appwrite.

### Resultado local — 6 de noviembre de 2026

- `assembleDebug`: correcto; APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
- `testDebugUnitTest`: 3 pruebas, 0 fallos y 0 errores (incluye la prueba básica preexistente).
- `lintDebug`: correcto, sin errores; permanecen 21 advertencias de dependencias, recursos y estilo/compatibilidad, incluyendo avisos en el SDK de Appwrite.
- `git diff --check`: sin errores de espacios.
- Se corrigió el estado del marcador de Maps para conservarlo con `remember` entre recomposiciones.

## Recorrido con dos cuentas reales

1. Registrar adoptante y refugio, cerrar sesión y entrar con cada cuenta. Reabrir y rotar la app: conservar la sesión y volver al flujo correcto.
2. Editar perfil/avatar; comprobar los cambios al volver al catálogo. Cortar Internet durante la subida y comprobar que se informa error.
3. Con refugio, crear mascota con coordenadas y fotos, editarla, filtrar catálogo y abrir detalle/mapa. Probar más de cinco fotos y coordenadas inválidas.
4. Con adoptante, enviar solicitud. Con refugio, verificar que aparece únicamente en su panel y que aceptar marca la mascota adoptada.
5. Abrir chat desde detalle, perfil y solicitud: comprobar que todos usan la misma conversación. Enviar desde ambas cuentas; verificar una sola burbuja por documento y conservación del texto ante fallo de envío.
6. Crear seguimiento con fotos y notas; reabrir la pantalla y comprobar persistencia. Repetir sin conectividad y verificar error.
7. Agendar cita futura y comprobar el recordatorio en la fecha indicada. Probar fecha inválida, notificaciones denegadas y alarmas exactas no autorizadas.
8. Cerrar sesión y entrar con otra cuenta: verificar que no se muestran solicitudes/citas de la sesión anterior.

## Requisitos de pruebas de integración

Se necesita un emulador/dispositivo conectado y acceso a Appwrite con las colecciones de `APPWRITE.md`. Durante esta migración no había dispositivos disponibles en `adb devices`; no se han ejecutado pruebas instrumentadas ni operaciones reales con cuentas del usuario. La clave de Maps y el esquema/permisos remotos requieren configuración y validación del propietario.
