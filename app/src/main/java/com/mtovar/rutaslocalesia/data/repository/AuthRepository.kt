package com.mtovar.rutaslocalesia.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthRepository {
    val currentUser: Boolean // True si ya está logueado
    suspend fun login(email: String, pass: String): Result<String>
    suspend fun signUp(email: String, pass: String): Result<String>
    fun logout()

    suspend fun sendEmailVerification(): Result<String>

    suspend fun reloadUser(): Result<Boolean>

    fun isEmailVerified(): Boolean
    suspend fun recoverPassword(email: String): Result<String>
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Boolean
        get() = auth.currentUser != null

    override suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Bienvenido de vuelta")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, pass: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success("Cuenta creada exitosamente")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    override suspend fun sendEmailVerification(): Result<String> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success("Se ha enviado un correo de verificación.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Boolean> {
        return try {
            auth.currentUser?.reload()?.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun recoverPassword(email: String): Result<String> {
        return try {
            // Esta función mágica de Firebase envía el correo automáticamente
            auth.sendPasswordResetEmail(email).await()
            Result.success("Te hemos enviado un correo para restablecer tu contraseña.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}