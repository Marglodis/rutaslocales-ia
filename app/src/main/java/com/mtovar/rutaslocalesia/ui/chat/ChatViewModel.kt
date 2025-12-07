package com.mtovar.rutaslocalesia.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.mtovar.rutaslocalesia.model.ChatMessage
import com.mtovar.rutaslocalesia.model.RespuestaRutas
import com.mtovar.rutaslocalesia.model.Ruta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {
    // Agrega este estado en tu ViewModel para controlar si mostramos el mapa
    private val _rutasEncontradas = MutableStateFlow<List<Ruta>>(emptyList())
    val rutasEncontradas = _rutasEncontradas.asStateFlow()
    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Historial de chat para mantener contexto con Gemini
    private val chat = generativeModel.startChat(
        history = listOf() // Se puede precargar mensajes aquí si es necesario
    )

    init {
        // Mensaje de bienvenida
        _messages.value = listOf(
            ChatMessage(text = "¡Hola! Soy Eco 🌿. ¿Qué tipo de aventura buscas hoy? (Tranquila, ejercicio, con perro...)", isUser = false)
        )
    }

    fun sendMessage(userText: String) {
        viewModelScope.launch {
            // Usar .value += no es thread-safe, mejor crear una lista nueva o usar update (si tienes stateflow reciente)
            val currentList = _messages.value.toMutableList()
            currentList.add(ChatMessage(text = userText, isUser = true))
            _messages.value = currentList

            _isLoading.value = true

            try {
                val promptMejorado = """
            El usuario dice: "$userText".
            Si pide rutas, responde como Eco y añade al final:
            ```json
            { "rutas": [ { "nombre": "...", "descripcion": "...", "latitud": 0.0, "longitud": 0.0 } ] }
            ```
            Si no, solo texto.
            """.trimIndent()

                val response = chat.sendMessage(promptMejorado)
                val fullText = response.text ?: ""

                var displayText = fullText

                // Verificación más robusta
                if (fullText.contains("rutas") && fullText.contains("{")) {
                    try {
                        val jsonString = extractJson(fullText)

                        // Solo intentamos parsear si parece un JSON válido (no está vacío)
                        if (jsonString.length > 2) {
                            val datos = jsonParser.decodeFromString<RespuestaRutas>(jsonString)

                            if (datos.rutas.isNotEmpty()) {
                                _rutasEncontradas.value = datos.rutas

                                // Limpieza visual: Quitamos el JSON y las etiquetas markdown del texto que ve el usuario
                                displayText = fullText
                                    .replace(jsonString, "") // Quitamos el JSON extraido
                                    .replace("```json", "")  // Quitamos etiqueta apertura
                                    .replace("```", "")      // Quitamos etiqueta cierre
                                    .trim()
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parseando JSON: ${e.message}")
                        // Fallo silencioso: el usuario ve el texto completo, no crashea
                    }
                }

                // Actualizamos UI con la respuesta limpia
                val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage(text = displayText, isUser = false))
                _messages.value = updatedList

            } catch (e: Exception) {
                val errorList = _messages.value.toMutableList()
                errorList.add(ChatMessage(text = "Error: ${e.localizedMessage}", isUser = false))
                _messages.value = errorList
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Función auxiliar para limpiar el JSON que a veces Gemini envuelve en markdown
    fun extractJson(text: String): String {
        // 1. Intentar encontrar el bloque de código Markdown primero (Más seguro)
        // El patrón busca ```json, luego captura todo el contenido ({...}) y cierra con ```
        val regex = """```json\s*(\{[\s\S]*?\})\s*```""".toRegex()

        val matchResult = regex.find(text)
        if (matchResult != null) {
            // Devolvemos solo el contenido capturado (el JSON puro)
            return matchResult.groupValues[1]
        }

        // 2. Fallback: Si no hay markdown, buscamos las llaves extremas
        val startIndex = text.indexOf("{")
        val endIndex = text.lastIndexOf("}")
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return text.substring(startIndex, endIndex + 1)
        }

        // 3. Si no encuentra nada, devolvemos un JSON vacío o lanzamos error controlado
        return "{}"
    }

    // Configuración recomendada para respuestas de IA
    val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true // Permite JSON un poco más relajado
    }
}
