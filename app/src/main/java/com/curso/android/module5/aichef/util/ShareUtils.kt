package com.curso.android.module5.aichef.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * =============================================================================
 * ShareUtils - Utilidad para compartir imágenes
 * =============================================================================
 * * Esta clase toma el Bitmap generado por la pantalla, lo guarda temporalmente
 * en la memoria caché y usa el FileProvider para abrir el menú nativo de
 * Android para compartir (WhatsApp, Telegram, etc.).
 */
object ShareUtils {

    fun shareRecipeImage(context: Context, bitmap: Bitmap) {
        try {
            // 1. Crear la carpeta "images" en la memoria caché
            // (¡Esto es exactamente lo que configuramos en file_paths.xml!)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // Crea la carpeta si no existe

            // 2. Crear el archivo temporal dentro de esa carpeta
            val file = File(cachePath, "shared_recipe.png")
            val stream = FileOutputStream(file)

            // Comprimir el Bitmap y guardarlo en el archivo
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            // 3. Obtener el "Pase VIP" (URI segura) usando nuestro FileProvider
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // 4. Configurar el "Intent" (la orden para Android)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                // Esto es crucial: le da permiso a WhatsApp/Telegram de leer la foto
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 5. Mostrar la pantalla nativa de "Compartir con..."
            context.startActivity(Intent.createChooser(shareIntent, "Compartir deliciosa receta con..."))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}