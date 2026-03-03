# AI Chef - Challenge Lab

## Sobre el proyecto
AI Chef es una aplicación móvil desarrollada en Android con Jetpack Compose que utiliza Inteligencia Artificial (en este caso Gemini) para generar recetas de cocina a partir de fotografías de ingredientes. 

Este repositorio contiene la resolución del *Challenge Lab*, enfocado en implementar funcionalidades de sincronización en la nube y utilidades nativas del sistema operativo, funcionalidades que ya habíamos implementado en
laboratorios anteriores como tener un ícono de corazón para los favoritos, o nuevas funcionalidades como el compartir nuestra receta en formato de imagen para otras plataformas como WhatsApp, Telegram, etc.

## Características implementadas 

### Para la parte 1: Sistema de Favoritos en Tiempo Real
* **Modelo de datos actualizado:** Inclusión del campo `isFavorite` en la estructura de la receta.
* **Sincronización con Firestore:** Actualización en tiempo real de los documentos en la base de datos mediante `toggleFavorite()`.
* **Interfaz Optimista (Optimistic UI):** Los cambios visuales en los íconos de corazón se reflejan instantáneamente para el usuario, manejando las peticiones a la nube en segundo plano.
* **Filtro dinámico:** Visualización exclusiva de recetas marcadas como favoritas con un solo toque.

### Para la parte 2: Compartir Recetas (Native Share Sheet)
* **Captura de Composable:** Renderizado de la pantalla completa de la receta (incluyendo la foto generada por IA, título, ingredientes y pasos) hacia un formato `Bitmap` utilizando `Picture` y `drawWithCache`.
* **FileProvider y Seguridad:** Configuración de rutas de caché (`file_paths.xml`) para permitir compartir archivos de forma segura.
* **Limpieza automática:** Almacenamiento temporal de las imágenes en la memoria caché para que Android las elimine automáticamente y no sature el dispositivo.
* **Integración Nativa:** Uso de `Intent.ACTION_SEND` para abrir el menú de compartir nativo, compatible con WhatsApp, Telegram, Email, etc.

## Tecnologías utilizadas
* Kotlin & Jetpack Compose.
* Firebase (Firestore & Storage).
* Coil (Carga asíncrona y caché de imágenes).
* Google Gemini AI.

---
### Enlace del video explicativo:
> (https://youtu.be/FR_hKxY3L78) 
