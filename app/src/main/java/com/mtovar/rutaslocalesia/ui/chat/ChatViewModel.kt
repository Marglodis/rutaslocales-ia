package com.mtovar.rutaslocalesia.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.mtovar.rutaslocalesia.data.local.RutasDao
import com.mtovar.rutaslocalesia.model.ChatMessage
import com.mtovar.rutaslocalesia.model.RespuestaRutas
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.model.entity.toEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val rutasDao: RutasDao
) : ViewModel() {

    private val _rutasEncontradas = MutableStateFlow<List<Ruta>>(emptyList())
    val rutasEncontradas = _rutasEncontradas.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val chat = generativeModel.startChat(history = listOf())

    // Configuración para respuestas de IA (JSON lenient)
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        _messages.value = listOf(
            ChatMessage(
                text = "¡Hola! Soy Eco 🌿. ¿Qué tipo de aventura buscas hoy? (Tranquila, ejercicio, con perro...)",
                isUser = false
            )
        )
    }

    fun sendMessage(userText: String) {
        viewModelScope.launch {
            // Agregar mensaje de usuario a la lista de forma segura
            val currentList = _messages.value.toMutableList()
            currentList.add(ChatMessage(text = userText, isUser = true))
            _messages.value = currentList

            _isLoading.value = true

            try {
                val promptMejorado = """
                El usuario dice: "$userText".
                
                Si pide recomendaciones:
                1. Responde amablemente como Eco (texto normal, motivador).
                2. AL FINAL, añade un JSON ESTRICTO con esta estructura exacta para CADA ruta recomendada:
                {
                  "rutas": [
                    { 
                      "nombre": "Nombre de la ruta", 
                      "descripcion": "Descripción evocadora y detallada...", 
                      "latitud": -33.4, 
                      "longitud": -70.6,
                      "dificultad": "Media",
                      "duracion": "45 min",
                      "rating": 4.8,
                      "tags": ["Sombra", "Vistas", "Río"],
                      "keywordImagen": "forest" 
                    }
                  ]
                }
                """.trimIndent()

                val response = chat.sendMessage(promptMejorado)
                val fullText = response.text ?: ""

                var displayText = fullText

                // Lógica de Parseo JSON
                if (fullText.contains("rutas") && fullText.contains("{")) {
                    try {
                        val jsonString = extractJson(fullText)
                        if (jsonString.length > 2) {
                            val datos = jsonParser.decodeFromString<RespuestaRutas>(jsonString)
                            if (datos.rutas.isNotEmpty()) {
                                _rutasEncontradas.value = datos.rutas

                                // Limpiamos el texto para la UI
                                displayText = fullText
                                    .replace(jsonString, "")
                                    .replace("```json", "")
                                    .replace("```", "")
                                    .trim()
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parseando JSON: ${e.message}")
                    }
                }

                // Agregar respuesta de Eco
                val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage(text = displayText, isUser = false))
                _messages.value = updatedList

            } catch (e: Exception) {
                // --- 🔍 LOG DE DEPURACIÓN ---
                // Esto imprimirá el error exacto en el Logcat
                Log.e("GEMINI_DEBUG", "❌ ERROR API: ${e.localizedMessage}")
                e.printStackTrace() // Imprime toda la traza técnica por si acaso
                // --- MANEJO DE ERRORES PERSONALIZADO PARA ECO ---
                val errorMsg = e.localizedMessage ?: ""

                val ecoMessage = if (errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("429")) {
                    // Confirmamos en el log que entramos aquí
                    Log.d("GEMINI_DEBUG", "✅ Detectado error de CUOTA (Rate Limit)")
                    // Error de Rate Limit (Plan gratuito)
                    "¡Uf! He caminado muy rápido y necesito recuperar el aliento. 😰\n\nDame unos segundos para descansar antes de seguir explorando rutas contigo. ⏱️🌿"
                } else if (errorMsg.contains("network") || errorMsg.contains("timeout") || errorMsg.contains("host")) {
                    Log.d("GEMINI_DEBUG", "✅ Detectado error de CONEXIÓN")
                    // Error de Internet
                    "Parece que perdimos la señal del sendero. Revisa tu conexión a internet. 📶❌"
                } else {
                    Log.d("GEMINI_DEBUG", "⚠️ Error desconocido: $errorMsg")
                    // Error genérico
                    "Lo siento, me he tropezado con una piedra desconocida. Intenta de nuevo. (Error técnico: $errorMsg)"
                }

                val errorList = _messages.value.toMutableList()
                errorList.add(ChatMessage(text = ecoMessage, isUser = false))
                _messages.value = errorList

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarRutaFavorita(ruta: Ruta) {
        viewModelScope.launch {
            try {
                rutasDao.guardarRuta(ruta.toEntity())
                // Feedback visual en el chat (usando la lista mutable para seguridad)
                val confirmList = _messages.value.toMutableList()
                confirmList.add(ChatMessage(text = "¡Ruta '${ruta.nombre}' guardada en favoritos! ⭐", isUser = false))
                _messages.value = confirmList
            } catch (e: Exception) {
                // Manejo de error al guardar (opcional)
            }
        }
    }

    fun extractJson(text: String): String {
        val regex = """```json\s*(\{[\s\S]*?\})\s*```""".toRegex()
        val matchResult = regex.find(text)
        if (matchResult != null) return matchResult.groupValues[1]

        val startIndex = text.indexOf("{")
        val endIndex = text.lastIndexOf("}")
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return text.substring(startIndex, endIndex + 1)
        }
        return "{}"
    }
}