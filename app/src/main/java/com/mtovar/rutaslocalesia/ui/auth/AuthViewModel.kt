package com.mtovar.rutaslocalesia.ui.auth

import android.util.Patterns.EMAIL_ADDRESS
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtovar.rutaslocalesia.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    // Estados
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // Mensajes informativos (éxito de envío de correos)
    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage = _authMessage.asStateFlow()

    // --- INICIO DE SESIÓN ---
    fun loginUser(email: String, pass: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || pass.isBlank()) {
            _authError.value = "Por favor llena todos los campos"
            return
        }

        // Limpiamos mensajes anteriores
        _authMessage.value = null
        _authError.value = null

        viewModelScope.launch {
            _isLoading.value = true

            val result = repo.login(cleanEmail, pass)

            result.onSuccess {
                // VERIFICACIÓN DE CORREO:
                // Antes de dejar pasar, revisamos si el email está verificado en Firebase
                // A veces es necesario recargar el usuario para obtener el estado más reciente
                repo.reloadUser()

                if (repo.isEmailVerified()) {
                    _isLoading.value = false
                    _isLoggedIn.value = true // ¡Pasa al chat!
                } else {
                    // Si no ha validado, cerramos la sesión técnica y mostramos error
                    repo.logout()
                    _isLoading.value = false
                    _authError.value = "Debes verificar tu correo para iniciar sesión. Revisa tu bandeja de entrada (y spam)."
                }
            }
            result.onFailure {
                _isLoading.value = false
                _authError.value = "Error de acceso: ${it.localizedMessage}"
            }
        }
    }

    // --- REGISTRO DE USUARIO ---
    fun registerUser(email: String, pass: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || pass.isBlank()) {
            _authError.value = "Por favor llena todos los campos"
            return
        }

        if (!isValidEmail(cleanEmail)) {
            _authError.value = "Formato de correo inválido."
            return
        }

        if (pass.length < 6) {
            _authError.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        _authMessage.value = null
        _authError.value = null

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Crear usuario en Firebase
            val result = repo.signUp(cleanEmail, pass)

            result.onSuccess {
                // 2. Enviar correo de verificación inmediatamente
                val verificationResult = repo.sendEmailVerification()
                repo.logout()
                _isLoading.value = false

                verificationResult.onSuccess {
                    _authMessage.value = "Cuenta creada con éxito. 📧 Hemos enviado un correo de verificación. Confírmalo antes de iniciar sesión."
                    // NOTA: No ponemos _isLoggedIn = true. El usuario debe verificar primero.
                }
                verificationResult.onFailure { error ->
                    _authMessage.value = "Cuenta creada, pero hubo un error enviando el correo de verificación: ${error.localizedMessage}"
                }
            }
            result.onFailure {
                _isLoading.value = false
                _authError.value = it.localizedMessage
            }
        }
    }

    // --- RECUPERACIÓN DE CONTRASEÑA (FALTABA ESTA FUNCIÓN) ---
    fun sendRecoveryEmail(email: String) {
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            _authError.value = "Ingresa un correo válido para recuperar la contraseña."
            return
        }

        _authMessage.value = null
        _authError.value = null

        viewModelScope.launch {
            _isLoading.value = true

            // Llamamos al repositorio (asegúrate de tener recoverPassword en tu AuthRepository)
            val result = repo.recoverPassword(cleanEmail)

            _isLoading.value = false

            result.onSuccess { msg ->
                _authMessage.value = msg // "Te hemos enviado un correo..."
            }
            result.onFailure {
                _authError.value = "Error al enviar correo: ${it.localizedMessage}"
            }
        }
    }

    // --- REENVIAR VERIFICACIÓN ---
    fun resendVerification() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.sendEmailVerification()
            _isLoading.value = false

            result.onSuccess {
                _authMessage.value = "Correo de verificación reenviado. Revisa tu bandeja."
                _authError.value = null // Limpiamos el error si lo había
            }
            result.onFailure { _authError.value = it.localizedMessage }
        }
    }

    // Validación auxiliar
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_ADDRESS.matcher(email).matches()
    }
}