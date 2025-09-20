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
    - (Se recomienda listar las bibliotecas específicas más importantes si es posible, ej:
      `androidx.core:core-ktx` a `1.17.0`).
- **Recursos:** Nombre de la aplicación en `strings.xml` (`app_name`) cambiado de "VITALARM" a "
  Vitalarm".
- **Manifiesto:** `AndroidManifest.xml` actualizado para incluir
  `android:localeConfig="@xml/locales_config"` y reflejar los cambios en las versiones del SDK.
- **Configuración del IDE:** Se eliminó la declaración XML redundante
  `<?xml version="1.0" encoding="UTF-8"?>` de `.idea/misc.xml`.
