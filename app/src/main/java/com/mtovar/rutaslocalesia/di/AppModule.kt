package com.mtovar.rutaslocalesia.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.mtovar.rutaslocalesia.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // Configuración para que la respuesta sea creativa pero precisa
        val config = generationConfig {
            temperature = 0.7f
        }

        // Prompt del Sistema: Aquí definimos la personalidad
        val systemInstruction = """
            Eres 'Eco', un asistente experto en senderismo y naturaleza local.
            Tu tono es amigable, motivador y breve.
            Tus respuestas deben ser visuales y evocar la sensación de estar en la naturaleza.
            Si te preguntan por rutas, pregunta primero: 1. Tiempo disponible, 2. Compañía (perro/familia), 3. Nivel de energía.
            Usa emojis de naturaleza (🌿, 🏔️, 🐕) para hacer el texto atractivo.
        """.trimIndent()

        // Usamos gemini-2.5-flash porque es rápido y gratis
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = config,
            systemInstruction = com.google.ai.client.generativeai.type.content { text(systemInstruction) }
        )
    }
}