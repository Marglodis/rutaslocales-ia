package com.mtovar.rutaslocalesia.ui.favoritos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.ui.components.RutaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    rutasFavoritas: List<Ruta>,
    onBack: () -> Unit,
    onItemClick: (Ruta) -> Unit,     // <--- Callback para navegar
    onDeleteClick: (Ruta) -> Unit    // <--- Callback para borrar
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Rutas Favoritas ❤️") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (rutasFavoritas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no tienes rutas guardadas 🍂")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(rutasFavoritas) { ruta ->
                    // Reutilizamos la tarjeta.
                    RutaCard(
                        ruta = ruta,
                        isFavoriteInitial = true, // <--- IMPORTANTE: Empieza marcado en rojo
                        onFavoriteClick = {
                            // Si desmarcas el corazón en esta pantalla, se borra
                            onDeleteClick(ruta)
                        },
                        onItemClick = { onItemClick(ruta) }, // Pasamos el click al padre
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}