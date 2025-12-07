package com.mtovar.rutaslocalesia.model

import kotlinx.serialization.Serializable

@Serializable
data class RespuestaRutas(
    val rutas: List<Ruta>
)