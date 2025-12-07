package com.mtovar.rutaslocalesia.model

// Modelo de datos simple para el mensaje
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)
