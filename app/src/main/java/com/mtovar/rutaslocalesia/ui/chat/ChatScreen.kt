package com.mtovar.rutaslocalesia.ui.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mtovar.rutaslocalesia.model.ChatMessage
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.ui.components.RutaCard
import com.mtovar.rutaslocalesia.ui.detail.RouteDetailScreen
import com.mtovar.rutaslocalesia.ui.favoritos.FavoritosScreen
import com.mtovar.rutaslocalesia.ui.map.MapViewContainer

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.refreshUser()
    }

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val rutas by viewModel.rutasEncontradas.collectAsState()
    val favoritos by viewModel.misFavoritos.collectAsState()

    var selectedRuta by remember { mutableStateOf<Ruta?>(null) }
    var showFavoritos by remember { mutableStateOf(false) }
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email?.substringBefore("@") ?: "Viajero"

    var isResultsExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(rutas) {
        if (rutas.isNotEmpty()) {
            isResultsExpanded = true
        }
    }

    if (showFavoritos) {
        FavoritosScreen(
            rutasFavoritas = favoritos,
            onBack = { showFavoritos = false },
            onItemClick = { rutaSeleccionada ->
                selectedRuta = rutaSeleccionada
                showFavoritos = false
            },
            onDeleteClick = { rutaParaBorrar ->
                viewModel.eliminarRutaFavorita(rutaParaBorrar)
            }
        )
        return
    }

    if (selectedRuta != null) {
        RouteDetailScreen(
            ruta = selectedRuta!!,
            onBack = { selectedRuta = null }
        )
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                        )
                    )
            ) {
                Column {
                    // ESPACIO PARA LA BARRA DE ESTADO (Batería, Hora)
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                    // CONTENIDO REAL (Ahora de 56dp estándar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp) // Altura estándar más elegante
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // IZQUIERDA: Título y Usuario
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Eco, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    "Eco IA",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Hola, $userEmail",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // DERECHA: Botones
                        Row {
                            IconButton(onClick = { showFavoritos = true }) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "Favoritos",
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = {
                                viewModel.logout()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Cerrar Sesión",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- ZONA SUPERIOR: RESULTADOS (ACORDEÓN) ---
            if (rutas.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // A) BARRA DE TÍTULO LLAMATIVA (TIPO TARJETA)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable { isResultsExpanded = !isResultsExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9) // Verde muy suave (Eco Light)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Icono de mapa para dar contexto
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isResultsExpanded) "Rutas Encontradas" else "Ver ${rutas.size} rutas en mapa",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF1B5E20), // Verde oscuro para contraste
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Icon(
                                imageVector = if (isResultsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Colapsar",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                    }

                    // B) CONTENIDO COLAPSABLE
                    AnimatedVisibility(
                        visible = isResultsExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            // EL MAPA
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                MapViewContainer(
                                    rutas = rutas,
                                    onMarkerClick = { rutaClickeada ->
                                        selectedRuta = rutaClickeada
                                    },
                                    isLocationEnabled = hasLocationPermission
                                )
                            }

                            // CARRUSEL
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                items(rutas) { ruta ->
                                    val isAlreadyFavorite =
                                        favoritos.any { it.nombre == ruta.nombre }
                                    RutaCard(
                                        ruta = ruta,
                                        isFavoriteInitial = isAlreadyFavorite,
                                        onFavoriteClick = { rutaClickeada ->
                                            if (isAlreadyFavorite) viewModel.eliminarRutaFavorita(
                                                rutaClickeada
                                            )
                                            else viewModel.guardarRutaFavorita(rutaClickeada)
                                        },
                                        onItemClick = { rutaParaDetalle ->
                                            selectedRuta = rutaParaDetalle
                                        }
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // --- ZONA INFERIOR: CHAT ---
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    ) {
                        items(messages) { message -> MessageBubble(message) }
                        if (isLoading) {
                            item { TypingIndicator() }
                        }
                    }

                    ChatInputArea(
                        onSend = { viewModel.sendMessage(it) },
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val isError =
        !isUser && (message.text.contains("recuperar el aliento") || message.text.contains("Error"))

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .background(
                        when {
                            isUser -> Color(0xFFDCF8C6)
                            isError -> Color(0xFFFFEBEE)
                            else -> Color.White
                        }
                    )
                    .padding(16.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isError) Color(0xFFB71C1C) else Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = if (isUser) "Tú" else "Guía IA",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun ChatInputArea(onSend: (String) -> Unit, enabled: Boolean) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Pregunta por una ruta...") },
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.LightGray
            )
        )

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            },
            enabled = enabled && text.isNotBlank(),
            modifier = Modifier
                .background(
                    if (enabled && text.isNotBlank()) Color(0xFF4CAF50) else Color.LightGray,
                    CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🌿", modifier = Modifier.padding(end = 4.dp))
        Text(
            "Eco está pensando...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}