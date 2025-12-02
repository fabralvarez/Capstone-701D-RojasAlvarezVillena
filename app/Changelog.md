## [0.9.0] - 2025-12-02

### Added

- **Interfaz de Usuario (UI):**
    - Implementacion de elementos graficos de Material 3 Expressive.
    - Implementacion de Fragments para mejorar rendimiento y navegabilidad a traves de la app.
    - Animaciones de Transicion entre Fragments para asegurar visuales más pulidas.
    - Implementacion de Configuraciones a nivel global de la app, incorporando la opcion de
      habilitar el tema de Material You.

### Changed

- **Interfaz de Usuario (UI):**
    - Actualización de las Activities del proyecto para adoptar el tema de
      Material 3 Expressive de forma paulatina.
    - Eliminacion de Toasts en favor del uso de Snackbars.
    - Overhaul de LanMenuActivity para mejorar la experiencia del usuario.

## [0.8.640] - 2025-11-28

### Changed

- **Interfaz de Usuario (UI):**
    - Actualización y Reorganización de las Activities del proyecto para utilizar el tema de
      Material Design 3 de forma homogénea.

## [0.8.1] - 2025-11-24

### Changed

- **Librerias y dependencias:**
    - Actualización de Gradle a la última versión estable.
    - Actualización de las dependencias de la aplicación a la última versión estable.

## [0.8.0] - 2025-11-24

### Changed

- **Estructura del Modelo de Datos:**
    - Bugfixes en el login de usuarios.

### Added

- **Funciones de Autenticación:**
    - Persistencia en el login de usuarios.
    - Integración con Google Password Manager, para mayor facilidad de guardado de contraseñas.
    - Autofill en el login de usuarios ahora disponible.

## [0.6.30] - 2025-11-24

### Changed

- **Estructura del Modelo de Datos:**
    - Bugfixes en el registro de nuevos usuarios.

## [0.6.0] - 2025-11-22

### Changed

- **Estructura del Modelo de Datos:**
    - Limpieza y reestructuración general del código fuente.

## [0.5.0] - 2025-11-18

### Changed

- **Estructura del Proyecto:**
    - Limpieza y reestructuración general del código fuente.

## [0.4.0] - 2025-11-18

### Changed

- **Compilación del Proyecto:**
    - Actualización de Java y JVM desde Java 11 a Java 21, con el fin de optimizar el rendimiento
      general de la app.

## [0.3.0] - 2025-11-16

### Changed

- **Estructura del Proyecto:**
    - Aplicación de la arquitectura MVC (Model-View-Controller) a lo largo de todo el proyecto.

## [0.2.1] - 2025-11-15

### Changed

- **Temas Material Design 3:**
    - Actualización completa de los temas de la aplicación para utilizar la última versión de
      Material Design 3.

## [0.2.0] - 2025-11-14

### Changed

- **Librerias y dependencias:**
    - Limpieza general del codigo fuente.

## [0.1.2] - 2025-11-14

### Changed

- **Librerias y dependencias:**
    - Actualización de Gradle a la última versión estable.
    - Actualización y redeclaración de las dependencias de la aplicación.
    - Limpieza general del codigo fuente.

## [0.1.1] - 2025-11-14

### Changed

- **Librerias y dependencias:**
    - Actualización y redeclaración de las dependencias de la aplicación.
    - Actualización de GMS a la última versión estable.

## [0.1.0] - 2025-11-14

### Changed

- **Librerias y dependencias:**
    - Actualización de las dependencias de la aplicación.
    - Actualización de AGP a la última versión estable.
    - Actualización de Kotlin a la última versión estable.

## [0.03.0] - 2025-09-20

### Added

- **Temas Material Design 3:**
    - Nuevos archivos de recursos `colors.xml` y `themes.xml` (y sus variantes `-night`) para
      implementar la paleta de colores y temas de Material Design 3.
    - Nuevos archivos `theme_overlays.xml` (y sus variantes `-night`) para definir estilos de
      contraste medio y alto.
- **Recursos de Strings:**
    - Nuevas strings en `strings.xml` para elementos de la interfaz de usuario como "Menú
      Principal", "Ingresar Persona a cuidado", "Cerrar Sesión", etc.
- **Dependencias:**
    - Añadidas dependencias para `androidx.fragment` y `androidx.recyclerview` en
      `app/build.gradle.kts` y `gradle/libs.versions.toml`.
- **Configuración del IDE:**
    - Añadida la declaración `<?xml version="1.0" encoding="UTF-8"?>` a `.idea/misc.xml`.
    - Configurada una herramienta de visualización en `.idea/misc.xml`.
    - Añadidas nuevas palabras al diccionario del proyecto en `.idea/dictionaries/project.xml`.
- **Funcionalidad en Activities:**
    - Habilitado el modo edge-to-edge en `MainMenuActivity.kt`, `LoginActivity.kt`,
      `RegisterActivity.kt` y `MainActivity.kt`.
    - Añadidas funciones para verificar el modo oscuro en las Activities mencionadas.

### Changed

- **Temas y Estilos:**
    - Actualización completa de los temas de la aplicación para utilizar Material Design 3.
    - Los layouts existentes (`activity_main.xml`, `activity_main_menu.xml`, `activity_login.xml`,
      `activity_register.xml`) ahora utilizan los nuevos colores y componentes de Material Design (
      e.g., `MaterialButton`).
- **AndroidManifest:**
    - `AndroidManifest.xml` actualizado para referenciar el nuevo `AppTheme` de Material Design 3.
- **Recursos de Strings:**
    - Modificada la string `login` de "Ingresar" a "Login" en `strings.xml`.
-
    *

*Activities (`MainMenuActivity.kt`, `LoginActivity.kt`, `RegisterActivity.kt`, `MainActivity.kt`):
**

- Actualizada la inicialización de listeners: la función `setupClickListeners` fue reemplazada
  por `initListeners`.
- Ajustado el manejo del botón "Atrás" en algunas actividades.

### Removed

- **Temas Antiguos:**
    - Eliminados los archivos `colors.xml` y `themes.xml` (y su variante `-night`) anteriores.

## [0.02.1] - 2025-09-20

### Changed

- Mejorada la organización de comentarios en `app/build.gradle.kts`:
    - Agrupadas las dependencias de testing bajo un único comentario.
    - Agrupadas las librerías externas (incluyendo Gson) bajo un único comentario.

## [0.02.0] - 2025-09-20

### Added

- Soporte para configuración regional en Español (EE. UU.) (`es-US`) mediante `locales_config.xml`.
- Archivo `app/Changelog.md` para el seguimiento de cambios en el proyecto.
- Nuevos archivos de configuración del IDE para mejorar la experiencia de desarrollo (`.idea/.name`,
  `.idea/dictionaries/project.xml`, `.idea/appInsightsSettings.xml`,
  `.idea/AndroidProjectSystem.xml`, `.idea/deviceManager.xml`).

### Changed

- **Versiones del SDK de Android:**
    - `compileSdk` actualizado de `34` a `36`.
    - `minSdk` actualizado de `21` a `31`.
    - `targetSdk` actualizado de `34` a `36`.
- **Versión de la Aplicación:** Actualizada de `"1.0"` a `"0.02.0"`.
- **Gradle:**
    - Versión del Android Gradle Plugin actualizada (versión específica no detallada, pero implícita
      en la actualización de dependencias).
    - URL de distribución del wrapper de Gradle actualizada a `gradle-8.14.3-bin.zip`.
- **Dependencias:**
    - Versión de Kotlin actualizada.
    - Actualizadas varias versiones de bibliotecas de AndroidX.
    - (Se recomienda listar las bibliotecas específicas más importantes si es posible, ej.:
      `androidx.core:core-ktx` a `1.17.0`).
- **Recursos:** Nombre de la aplicación en `strings.xml` (`app_name`) cambiado de "VITALARM" a "
  Vitalarm".
- **Manifiesto:** `AndroidManifest.xml` actualizado para incluir
  `android:localeConfig="@xml/locales_config"` y reflejar los cambios en las versiones del SDK.
- **Configuración del IDE:** Se eliminó la declaración XML redundante
  `<?xml version="1.0" encoding="UTF-8"?>` de `.idea/misc.xml`.

## [0.01.0] - 2025-09-16

### Versión base
