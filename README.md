# ReportesCiudadano

App móvil para reportar incidentes ciudadanos desde tu celular. Desarrollada en **Kotlin** con **Jetpack Compose** y base de datos local **SQLite**.

## 📱 Características

- **Autenticación:** registro e inicio de sesión de usuarios.
- **Feed de reportes:** visualiza los reportes publicados por la comunidad.
- **Nuevo reporte:** crea reportes con foto, título, descripción y ubicación.
- **Detalle de reporte:** ve la información completa de cada reporte.
- **Perfil de usuario:** edita tu información personal.
- **Estadísticas:** visualiza gráficos de reportes por categoría y estado.
- **Notificaciones:** centro de notificaciones dentro de la app.
- **Mapas:** integración con MapLibre y OpenStreetMap para visualizar ubicaciones.
- **Datos simulados:** incluye un módulo de datos de prueba para probar sin backend.

## 🛠️ Tecnologías

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Base de datos:** SQLite (local)
- **Mapas:** MapLibre Android, osmdroid
- **Imágenes:** Coil
- **Build:** Gradle (Kotlin DSL)
- **SDK mínimo:** API 24 (Android 7.0)
- **SDK objetivo:** API 37

## 🚀 Cómo ejecutar el proyecto

### Requisitos
- [Android Studio](https://developer.android.com/studio) instalado (última versión recomendada).
- SDK de Android API 37 instalado.
- Un dispositivo físico o emulador con API 24 o superior.

### Pasos

1. Clona este repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/ReportesCiudadano.git
   cd ReportesCiudadano
   ```

2. Abre el proyecto en Android Studio:
   - `File` → `Open` → selecciona la carpeta `ReportesCiudadano`.

3. Sincroniza el proyecto con Gradle:
   - Android Studio lo hará automáticamente al abrirlo, o usa `File` → `Sync Project with Gradle Files`.

4. Ejecuta la app:
   - Selecciona un dispositivo o emulador.
   - Haz clic en **Run** (▶) o presiona `Shift + F10`.

> ⚠️ **Nota:** El archivo `local.properties` (ruta del SDK) se genera automáticamente en tu máquina y **no se sube al repositorio**. Android Studio lo creará al sincronizar el proyecto.

## 📂 Estructura del proyecto

```
ReportesCiudadano/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/reportesciudadano/
│           │   ├── data/           # Base de datos SQLite y repositorio
│           │   ├── model/          # Modelos de datos (User, Report, etc.)
│           │   ├── ui/
│           │   │   ├── components/ # Componentes reutilizables (mapas, tarjetas, barras)
│           │   │   ├── screens/    # Pantallas (Login, Feed, Perfil, etc.)
│           │   │   └── theme/      # Tema y colores
│           │   └── utils/          # Utilidades (validaciones, notificaciones)
│           └── res/                # Recursos (layouts, drawables, valores)
├── build.gradle.kts                # Configuración de Gradle (nivel raíz)
├── settings.gradle.kts             # Módulos incluidos
└── gradle/
    └── libs.versions.toml          # Catálogo de dependencias
```

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Si deseas mejorar este proyecto:

1. Haz un fork del repositorio.
2. Crea una rama con tu feature (`git checkout -b feature/nueva-funcionalidad`).
3. Haz commit de tus cambios (`git commit -m "feat: descripcion"`).
4. Sube tu rama (`git push origin feature/nueva-funcionalidad`).
5. Abre un Pull Request.

## 👤 Autor

Desarrollado como proyecto universitario / personal de reportes ciudadanos.

---

> 💡 **Tip:** Si vas a publicar la app en producción, recuerda configurar el archivo `keystore.properties` para firmar el APK de release y nunca subas tus claves de firma al repositorio.
