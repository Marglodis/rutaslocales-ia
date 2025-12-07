package com.mtovar.rutaslocalesia.model

import kotlinx.serialization.Serializable

@Serializable
data class Ruta(
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double
)