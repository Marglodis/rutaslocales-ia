package com.mtovar.rutaslocalesia.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.ui.map.MapViewContainer
import kotlinx.coroutines.delay

@Composable
fun RouteDetailScreen(
    ruta: Ruta,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // OPTIMIZACIÓN CRÍTICA 1: "Remember" la solicitud de imagen.
    // Esto evita que Coil intente crear la solicitud una y otra vez si hay recomposiciones.
    val imageRequest = remember(ruta.keywordImagen) {
        ImageRequest.Builder(context)
            .data("https://picsum.photos/seed/${ruta.keywordImagen}/400/300")
            .crossfade(true)
            .size(800, 600) // Limitamos la decodificación
            .dispatcher(kotlinx.coroutines.Dispatchers.IO) // Forzamos IO
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }
    // OPTIMIZACIÓN 1: Pedimos una imagen más pequeña (400x300) para ahorrar memoria
    // val imageUrl = "https://picsum.photos/seed/${ruta.keywordImagen}/400/300"
    // OPTIMIZACIÓN 2: Estado para controlar cuándo cargar el mapa
    var showMap by remember { mutableStateOf(false) }

    // Efecto de "Lazy Load": Esperamos 500ms (lo que dura la transición) antes de cargar el mapa
    LaunchedEffect(Unit) {
        delay(500)
        showMap = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // 1. HEADER IMAGEN
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = imageRequest, // Usamos la request recordada
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }

            // 2. CONTENIDO
            Column(modifier = Modifier.padding(24.dp)) {
                // Título y Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ruta.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    RatingBadge(ruta.rating)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.Timer,
                        label = ruta.duracion,
                        color = Color(0xFFE3F2FD),
                        textColor = Color(0xFF1565C0)
                    )
                    InfoChip(
                        icon = Icons.Default.Terrain,
                        label = ruta.dificultad,
                        color = Color(0xFFE8F5E9),
                        textColor = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ruta.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Descripción
                Text(
                    text = "Sobre esta ruta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ruta.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. SECCIÓN MAPA ---
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (showMap) {
                        // A: EL MAPA (Solo carga después de los 700ms)
                        MapViewContainer(
                            rutas = listOf(ruta),
                            onMarkerClick = {},
                            liteMode = true // <--- ¡ESTO SALVARÁ EL RENDIMIENTO!
                        )
                    } else {
                        // B: LOADER (Se muestra mientras "descansa" la CPU)
                        // Loader simple
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Gray
                        )

                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Botón Atrás
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun RatingBadge(rating: Double) {
    Surface(color = Color(0xFFFFC107), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = rating.toString(), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, label: String, color: Color, textColor: Color) {
    Surface(color = color, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}