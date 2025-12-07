package com.mtovar.rutaslocalesia.model

import kotlinx.serialization.Serializable
@Serializable
data class Ruta(
    val id: String = java.util.UUID.randomUUID().toString(), // ID único para navegación
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    // Nuevos campos
    val dificultad: String, // "Fácil", "Media", "Difícil"
    val duracion: String,   // Ej: "2 horas"
    val rating: Double,     // Ej: 4.8
    val tags: List<String>, // Ej: ["Río", "Sombra", "Perros"]
    val keywordImagen: String // Una palabra clave para buscar la foto (ej: "forest", "mountain")
)