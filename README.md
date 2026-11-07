# AdoptaAmor — AppMovAdopDeMascotas

Aplicación Android con Kotlin, Jetpack Compose, MVVM y Appwrite real.

La migración de `APP_Mascota_Prueva.zip` se desarrolla en **`master`**.

## Funcionalidades

- Registro/login por correo y roles Adoptante/Refugio.
- Perfil con avatar, publicaciones, catálogo, filtros y detalle con mapa.
- Solicitudes de adopción, citas y recordatorios locales.
- Chat con Appwrite Realtime y seguimiento posadopción con fotos.

## Ejecutar

1. Abrir el proyecto con Android Studio compatible con AGP 9.3.2.
2. Instalar Android SDK 37. El wrapper usa Gradle 9.5.0 y el criterio JVM del proyecto requiere JDK 25.
3. Configurar Appwrite siguiendo [docs/APPWRITE.md](docs/APPWRITE.md).
4. Opcional: definir `MAPS_API_KEY=...` en `~/.gradle/gradle.properties`. Sin clave se muestra un aviso y se conserva el botón para abrir una app de mapas.
5. Ejecutar en Android 8.0/API 26 o superior.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Documentación

- [Módulos migrados, equivalencias e inyección de dependencias](docs/MIGRACION.md).
- [Colecciones, atributos y configuración de Appwrite](docs/APPWRITE.md).
- [Pruebas y recorrido de integración](docs/VALIDACION.md).

El acceso Google/Apple y las cuentas demo no están habilitados en el flujo real. Las notificaciones son locales, no notificaciones push a otros dispositivos.
