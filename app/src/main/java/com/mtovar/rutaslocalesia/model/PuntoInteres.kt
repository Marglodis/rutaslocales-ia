package com.mtovar.rutaslocalesia.model

import kotlinx.serialization.Serializable

@Serializable
data class PuntoInteres(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String // Ej: "foto", "parking", "inicio"
)
