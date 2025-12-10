package com.mtovar.rutaslocalesia.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.mtovar.rutaslocalesia.BuildConfig
import com.mtovar.rutaslocalesia.data.local.RutasDao
import com.mtovar.rutaslocalesia.model.ChatMessage
import com.mtovar.rutaslocalesia.model.RespuestaRutas
import com.mtovar.rutaslocalesia.model.Ruta
import com.mtovar.rutaslocalesia.model.entity.toEntity
import com.mtovar.rutaslocalesia.model.entity.toRuta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val rutasDao: RutasDao,
    private val auth: FirebaseAuth
) : ViewModel() {
    // Controlamos quién es el usuario actual
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)

    // Si _currentUserId cambia, la consulta a la DB cambia sola
    @OptIn(ExperimentalCoroutinesApi::class)
    val misFavoritos = _currentUserId.flatMapLatest { userId ->
        if (userId != null) {
            rutasDao.obtenerTodas(userId).map { list -> list.map { it.toRuta() } }
        } else {
            flowOf(emptyList()) // Si no hay usuario, lista vacía
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
        // 1. VALIDACIÓN PREVENTIVA DE API KEY
        // Verificamos si la clave es nula, está vacía o es la por defecto
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "null" || apiKey.contains("TU_CLAVE")) {
            val errorList = _messages.value.toMutableList()
            errorList.add(ChatMessage(text = userText, isUser = true))
            errorList.add(
                ChatMessage(
                    text = "⚠️ **Error de Configuración:**\n\nNo se detectó una API Key válida. Por favor, configura tu clave de Gemini en el archivo local.properties y recompila la app.",
                    isUser = false
                )
            )
            _messages.value = errorList
            return
        }
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
                1. Responde amablemente como Eco.
                2. AL FINAL, añade un JSON ESTRICTO con esta estructura, incluyendo 2 o 3 puntos de interés cercanos (miradores, parking, hitos):
                {
                  "rutas": [
                    { 
                      "nombre": "Nombre", 
                      "descripcion": "...", 
                      "latitud": -33.0, 
                      "longitud": -70.0,
                      "dificultad": "Media",
                      "duracion": "...",
                      "rating": 4.5,
                      "tags": ["..."],
                      "keywordImagen": "mountain",
                      "puntosInteres": [
                         { "nombre": "Estacionamiento", "latitud": -33.01, "longitud": -70.01, "tipo": "parking" },
                         { "nombre": "Mirador Las Águilas", "latitud": -33.02, "longitud": -70.02, "tipo": "foto" }
                      ]
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

                // MENSAJES DE ERROR AMIGABLES
                val ecoMessage = when {
                    // 1. Error de API Key (403)
                    errorMsg.contains("403") || errorMsg.contains("API key") -> {
                        "🔒 **Problema de Acceso:**\nLa llave de seguridad (API Key) no es válida o falta. Revisa tu configuración."
                    }

                    // 2. NUEVO: Servidor Saturado (503)
                    // Detectamos si el mensaje menciona "503" o "overloaded"
                    errorMsg.contains("503") || errorMsg.contains("overloaded", ignoreCase = true) -> {
                        "🐌 **Tráfico Intenso:**\nLos servidores de IA están muy solicitados en este momento (Error 503). Por favor, espera unos segundos e inténtalo de nuevo."
                    }

                    // 3. Cuota excedida (429)
                    errorMsg.contains("429") || errorMsg.contains("quota") -> {
                        "¡Uf! He caminado muy rápido y necesito recuperar el aliento. 😰\n\nDame unos segundos para descansar."
                    }

                    // 4. Error de Serialización (El crash técnico)
                    // Si cae aquí, es porque el servidor mandó algo raro que no es 503 explícito
                    e is SerializationException || errorMsg.contains("MissingFieldException") -> {
                        "🧩 **Error de Conexión:**\nRecibí una respuesta inesperada del servidor. Intenta de nuevo."
                    }

                    // 5. Sin internet
                    errorMsg.contains("network") || errorMsg.contains("host") -> {
                        "Parece que perdimos la señal del sendero. Revisa tu conexión a internet. 📶❌"
                    }

                    // 6. Genérico
                    else -> "Lo siento, me he tropezado con una piedra desconocida. (Error: $errorMsg)"
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
        val uid = auth.currentUser?.uid ?: return // Seguridad
        viewModelScope.launch {
            rutasDao.guardarRuta(ruta.toEntity(uid))
            // Feedback en el chat (opcional)
            val currentList = _messages.value.toMutableList()
            currentList.add(ChatMessage(text = "¡Guardada en tus favoritos! ⭐", isUser = false))
            _messages.value = currentList
        }
    }

    fun eliminarRutaFavorita(ruta: Ruta) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            rutasDao.eliminarPorNombre(ruta.nombre, uid)
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

    fun logout() {
        auth.signOut()
        // Limpieza de datos visuales
        _currentUserId.value = null
        _messages.value = listOf(ChatMessage(text = "¡Hola de nuevo! 🌿...", isUser = false))
        _rutasEncontradas.value = emptyList()
    }

    // REFRESH: Llamar al entrar al chat para asegurar que tenemos el ID
    fun refreshUser() {
        _currentUserId.value = auth.currentUser?.uid
    }
}