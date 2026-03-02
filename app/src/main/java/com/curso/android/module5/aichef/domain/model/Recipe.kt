package com.curso.android.module5.aichef.domain.model

/**
 * =============================================================================
 * Recipe - Modelo de dominio para recetas
 * =============================================================================
 *
 * Este modelo representa una receta generada por la IA.
 * Se usa tanto para la UI como para persistir en Firestore.
 */
data class Recipe(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUri: String = "",
    val generatedImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false // <-- NUEVO: Variable para favoritos
) {
    /**
     * Convierte el modelo a un Map para guardar en Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title,
        "ingredients" to ingredients,
        "steps" to steps,
        "imageUri" to imageUri,
        "generatedImageUrl" to generatedImageUrl,
        "createdAt" to createdAt,
        "isFavorite" to isFavorite // <-- LO NUEVO: Vamos a guardar en la base de datos.
    )

    companion object {
        /**
         * Crea una Recipe desde un documento de Firestore
         */
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(id: String, data: Map<String, Any?>): Recipe {
            return Recipe(
                id = id,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                ingredients = (data["ingredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                steps = (data["steps"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                imageUri = data["imageUri"] as? String ?: "",
                generatedImageUrl = data["generatedImageUrl"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                isFavorite = data["isFavorite"] as? Boolean ?: false // <-- NUEVO: Leer de la base de datos (por defecto false)
            )
        }
    }
}

/**
 * =============================================================================
 * GeneratedRecipe - Resultado del análisis de IA
 * =============================================================================
 */
data class GeneratedRecipe(
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>
)