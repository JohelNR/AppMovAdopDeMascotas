# Validación de la sincronización

## Comprobación de identidad con el ZIP

```bash
diff -rq --strip-trailing-cr --exclude=.git --exclude=.idea --exclude=docs \
  --exclude=build --exclude=.gradle --exclude=local.properties \
  APP_Mascota_Prueva/ AppMovAdopDeMascotas/
```

Resultado esperado: sin salida, es decir, idéntico al ZIP. Se verificó al preparar esta rama.

## Compilación

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --console=plain
```

El ZIP incluía la carpeta `build/` del compañero con clases y DEX compilados del 6 de noviembre de 2026, lo que indica que compiló en su equipo con esta misma configuración (AGP 9.5.0, Gradle 9.6.0). La compilación no se ejecutó en el entorno de esta sincronización porque no tiene el SDK de Android.

## Recorrido manual

1. Login: se ven "Continuar con Google" y "Acceso Rápido de Prueba (Demo)" con los botones Entrar Adoptante y Entrar Refugio.
2. Registro con correo real, cierre de sesión y nuevo login.
3. Catálogo: con la colección vacía se crean las mascotas de ejemplo; los filtros funcionan.
4. Detalle → solicitud → aceptar desde el refugio → la mascota pasa a ADOPTADA.
5. Agendar cita → llega una notificación unos 10 segundos después.
6. Chat → llega la respuesta automática del refugio a los 2 segundos.
7. Seguimiento con fotos.
