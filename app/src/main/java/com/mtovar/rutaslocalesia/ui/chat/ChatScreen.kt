package com.mtovar.rutaslocalesia.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Eco
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mtovar.rutaslocalesia.model.ChatMessage
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.ui.components.RutaCard
import com.mtovar.rutaslocalesia.ui.detail.RouteDetailScreen
import com.mtovar.rutaslocalesia.ui.map.MapViewContainer

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // Observamos las rutas encontradas
    val rutas by viewModel.rutasEncontradas.collectAsState()
    // NUEVO ESTADO: Ruta seleccionada
    var selectedRuta by remember { mutableStateOf<Ruta?>(null) }
    // Si hay una ruta seleccionada, mostramos el detalle ocupando toda la pantalla
    if (selectedRuta != null) {
        RouteDetailScreen(
            ruta = selectedRuta!!,
            onBack = { selectedRuta = null } // Al volver, limpiamos la selección
        )
        // Usamos return para no renderizar el chat debajo
        return
    }
    val listState = rememberLazyListState()

    // Scroll automático al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            // Header estilo Naturaleza
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Asistente Rutas IA",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ZONA SUPERIOR: Si hay rutas, mostramos el mapa ocupando espacio
            if (rutas.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. EL MAPA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Reduje un poco la altura para que quepan las cards
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        MapViewContainer(
                            rutas = rutas,
                            onMarkerClick = { rutaClickeada -> selectedRuta = rutaClickeada }
                        )
                        // Botón cerrar (opcional)
                        IconButton(
                            onClick = { /* Lógica para limpiar rutas */ },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) { /* Icono X */ }
                    }

                    // 2. CARRUSEL DE TARJETAS (NUEVO)
                    // Aquí es donde el usuario guarda en Room
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(rutas) { ruta ->
                            RutaCard(
                                ruta = ruta,
                                onFavoriteClick = { rutaParaGuardar ->
                                    // LLAMADA A ROOM:
                                    viewModel.guardarRutaFavorita(rutaParaGuardar)
                                },
                                onItemClick = { rutaParaDetalle ->
                                    selectedRuta = rutaParaDetalle
                                }
                            )
                        }
                    }
                }
            }

            // ZONA INFERIOR: El Chat
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
// Detectamos si es un mensaje de error de Eco
    val isError =
        !isUser && (message.text.contains("recuperar el aliento") || message.text.contains("Error"))
    // Animación de entrada
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
                            isUser -> Color(0xFFDCF8C6) // Verde Usuario
                            isError -> Color(0xFFFFEBEE) // Rojo suave para Error
                            else -> Color.White // Blanco normal Eco
                        }
                    )
                    .padding(16.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isError) Color(0xFFB71C1C) else Color.Black, // Texto rojo oscuro si es error
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // Pequeña etiqueta de "IA" o "Tú"
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