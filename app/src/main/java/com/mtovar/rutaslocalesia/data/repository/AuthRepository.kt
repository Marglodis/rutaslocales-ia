package com.mtovar.rutaslocalesia.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthRepository {
    val currentUser: Boolean // True si ya está logueado
    suspend fun login(email: String, pass: String): Result<String>
    suspend fun signUp(email: String, pass: String): Result<String>
    fun logout()
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
}