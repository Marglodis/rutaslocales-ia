package com.mtovar.rutaslocalesia.ui.auth

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

    // Estados simples
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Por favor llena todos los campos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = repo.login(email, pass)
            _isLoading.value = false

            result.onSuccess { _isLoggedIn.value = true }
            result.onFailure { _authError.value = it.localizedMessage }
        }
    }

    fun registerUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Por favor llena todos los campos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = repo.signUp(email, pass)
            _isLoading.value = false

            result.onSuccess { _isLoggedIn.value = true }
            result.onFailure { _authError.value = it.localizedMessage }
        }
    }
}