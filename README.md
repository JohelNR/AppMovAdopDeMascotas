# 🐾 AdoptaAmor - Aplicación Móvil de Adopción de Mascotas

Aplicación móvil nativa en **Android** desarrollada con **Kotlin** y **Jetpack Compose** para la gestión integral de adopciones de mascotas, conectando a adoptantes responsables con refugios y publicadores.

---

## 🚀 Características Principales

* **🔐 Autenticación y Perfiles (RF1, RF2):**
  * Inicio de sesión y registro de usuarios con roles diferenciados (*Adoptante* y *Refugio*).
  * Inicio de sesión social con Google y acceso rápido para demostración (demo).
  * Gestión de perfil con foto de avatar y estado de cuenta.

* **🐶 Catálogo y Publicaciones (RF3, RF4, RF5, RF6, RF7):**
  * Catálogo de mascotas con diseño en tarjetas interactivas y diseño Material 3.
  * Filtros en tiempo real por especie (perros, gatos, otros), tamaño y edad.
  * Búsqueda por nombre o raza.
  * Publicación, edición y eliminación de mascotas (exclusivo para refugios) con subida de fotos a la nube.
  * Vista de detalle completa con foto, descripción, edad, tamaño y estado de adopción.

* **📋 Solicitudes de Adopción y Citas (RF8, RF9, RF10, RF20):**
  * Formulario completo de solicitud de adopción con validaciones.
  * Panel de gestión de solicitudes para el refugio (Aprobar / Rechazar).
  * Agendamiento y confirmación de citas y entrevistas con recordatorios locales.

* **💬 Chat en Tiempo Real y Seguimiento (RF11, RF13):**
  * Mensajería instantánea en vivo entre adoptante y refugio asociada a la mascota.
  * Módulo de seguimiento post-adopción con reportes fotográficos de salud y bienestar.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Kotlin 2.2.x
* **UI:** Jetpack Compose con Material Design 3
* **Arquitectura:** MVVM (Model - View - ViewModel) con código y nomenclatura clara en español
* **Navegación:** Jetpack Navigation Compose con NavHost y rutas desacopladas
* **Backend y Base de Datos:** [Appwrite Cloud](https://appwrite.io/) (Autenticación, Base de Datos NoSQL, Storage y Realtime WebSockets)
* **Carga de Imágenes:** Coil Compose
* **Notificaciones:** AlarmManager y BroadcastReceiver nativos de Android

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/app_adopcion_d_mascotas/
├── 📦 datos/
│   ├── modelos/          # Clases de datos (Mascota, Usuario, Solicitud, Cita, etc.)
│   ├── remoto/           # Cliente singleton de Appwrite Cloud
│   └── repositorios/     # Lógica de datos y llamadas al SDK de Appwrite
├── 🗺️ navegacion/         # Grafo de navegación central (NavegacionApp.kt, Rutas.kt)
├── 🔔 notificaciones/    # Ayudante de notificaciones y recordatorios
└── 📱 ui/
    ├── componentes/      # Componentes reutilizables (FondoApp, TarjetaMascota)
    ├── modelovista/      # ViewModels de cada pantalla
    ├── pantallas/        # Vistas de la aplicación organizadas por módulo
    └── tema/             # Colores, tipografía y tema Material 3
```

---

## ⚙️ Requisitos para Ejecutar

1. **Android Studio** (Koala / Ladybug o superior).
2. **JDK 17** o **JDK 21**.
3. Dispositivo físico o emulador con **Android 8.0 (API 26)** o superior.
4. Conexión a Internet activa para sincronización en la nube con Appwrite.
