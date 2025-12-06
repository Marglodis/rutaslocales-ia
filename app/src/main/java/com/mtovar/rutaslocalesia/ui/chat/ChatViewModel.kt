package com.mtovar.rutaslocalesia.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Modelo de datos simple para el mensaje
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Historial de chat para mantener contexto con Gemini
    private val chat = generativeModel.startChat(
        history = listOf() // Puedes precargar mensajes aquí si quieres
    )

    init {
        // Mensaje de bienvenida
        _messages.value = listOf(
            ChatMessage(text = "¡Hola! Soy Eco 🌿. ¿Qué tipo de aventura buscas hoy? (Tranquila, ejercicio, con perro...)", isUser = false)
        )
    }

    fun sendMessage(userText: String) {
        viewModelScope.launch {
            // 1. Agregar mensaje del usuario a la UI
            val userMsg = ChatMessage(text = userText, isUser = true)
            _messages.value += userMsg
            _isLoading.value = true

            try {
                // 2. Enviar a Gemini y esperar respuesta
                val response = chat.sendMessage(userText)
                val aiResponseText = response.text ?: "Lo siento, no pude encontrar rutas en este momento."

                // 3. Agregar respuesta de IA a la UI
                val aiMsg = ChatMessage(text = aiResponseText, isUser = false)
                _messages.value += aiMsg

            } catch (e: Exception) {
                _messages.value += ChatMessage(text = "Error de conexión: ${e.localizedMessage}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}