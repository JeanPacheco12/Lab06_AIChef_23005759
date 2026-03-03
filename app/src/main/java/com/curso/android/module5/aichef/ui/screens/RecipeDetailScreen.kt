package com.curso.android.module5.aichef.ui.screens

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.curso.android.module5.aichef.domain.model.UiState
import com.curso.android.module5.aichef.ui.viewmodel.ChefViewModel
import com.curso.android.module5.aichef.util.ShareUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: ChefViewModel,
    recipeId: String,
    onNavigateBack: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val recipe = recipes.find { it.id == recipeId }
    val imageState by viewModel.imageGenerationState.collectAsStateWithLifecycle()

    // =========================================================================
    // VARIABLES PARA COMPARTIR RECETA (PARTE 2)
    // =========================================================================
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    // El "Picture" es el lienzo invisible donde se graba la interfaz
    val picture = remember { Picture() }

    LaunchedEffect(recipe) {
        recipe?.let {
            viewModel.generateRecipeImage(
                recipeId = it.id,
                existingImageUrl = it.generatedImageUrl,
                recipeTitle = it.title,
                ingredients = it.ingredients
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Detalle de Receta") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearImageState()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Si tenemos la receta, mostramos el botón de compartir
                    if (recipe != null) {
                        if (isSharing) {
                            // Animación de carga mientras prepara la imagen
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = {
                                isSharing = true
                                coroutineScope.launch {
                                    // Pausa de 100ms para asegurar que la UI muestre el círculo de carga
                                    delay(100)
                                    try {
                                        // 1. Convertir el Picture a Bitmap
                                        val bitmap = Bitmap.createBitmap(
                                            picture.width,
                                            picture.height,
                                            Bitmap.Config.ARGB_8888
                                        )
                                        val canvas = android.graphics.Canvas(bitmap)
                                        canvas.drawColor(android.graphics.Color.WHITE) // Fondo blanco
                                        canvas.drawPicture(picture)

                                        // 2. Enviar a ShareUtils
                                        ShareUtils.shareRecipeImage(context, bitmap)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isSharing = false
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir receta"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Receta no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ============================================================
                // BLOQUE A CAPTURAR (PARTE 2)
                // Envolvemos el contenido en esta columna con drawWithCache
                // ============================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithCache {
                            val width = this.size.width.toInt()
                            val height = this.size.height.toInt()
                            onDrawWithContent {
                                val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                    picture.beginRecording(width, height)
                                )
                                draw(this, this.layoutDirection, pictureCanvas, this.size) {
                                    this@onDrawWithContent.drawContent()
                                }
                                picture.endRecording()

                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawPicture(picture)
                                }
                            }
                        }
                        // Le damos un pequeño fondo blanco extra a la columna para que
                        // el PNG final no tenga texto flotando en transparencias.
                        .padding(8.dp)
                ) {
                    // Título extra para la imagen capturada
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    RecipeImageSection(
                        imageState = imageState,
                        recipeTitle = recipe.title,
                        onRetry = {
                            viewModel.generateRecipeImage(
                                recipeId = recipe.id,
                                existingImageUrl = "",
                                recipeTitle = recipe.title,
                                ingredients = recipe.ingredients
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    IngredientsSection(ingredients = recipe.ingredients)

                    Spacer(modifier = Modifier.height(24.dp))
                    StepsSection(steps = recipe.steps)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RecipeImageSection(
    imageState: UiState<String>,
    recipeTitle: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center
        ) {
            when (imageState) {
                is UiState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Preparando imagen...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is UiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = imageState.message ?: "Generando imagen con IA...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    AsyncImage(
                        model = imageState.data,
                        contentDescription = "Imagen de $recipeTitle",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = imageState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientsSection(ingredients: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsSection(steps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Preparación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}