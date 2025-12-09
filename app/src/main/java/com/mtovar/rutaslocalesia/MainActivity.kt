package com.mtovar.rutaslocalesia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.mtovar.rutaslocalesia.ui.auth.AuthScreen
import com.mtovar.rutaslocalesia.ui.chat.ChatScreen
import com.mtovar.rutaslocalesia.ui.theme.RutaslocalesiaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            RutaslocalesiaTheme()  {
                // ESTADO REACTIVO: Escucha directamente a Firebase
                var isUserLoggedIn by remember {
                    mutableStateOf(auth.currentUser != null && auth.currentUser?.isEmailVerified == true)
                }

                // EFECTO: Se suscribe a los cambios de sesión (Login/Logout)
                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser
                        // Solo dejamos pasar si hay usuario Y además su correo está verificado.
                        // Al registrarse, isEmailVerified es FALSE, así que isUserLoggedIn se mantiene FALSE.
                        isUserLoggedIn = user != null && user.isEmailVerified
                    }
                    auth.addAuthStateListener(listener)

                    // Limpieza cuando se destruye la actividad
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                // NAVEGACIÓN AUTOMÁTICA
                if (isUserLoggedIn) {
                    // Si Firebase dice que hay usuario, mostramos el Chat
                    ChatScreen()
                } else {
                    // Si no hay usuario, mostramos el Login
                    AuthScreen()
                }
            }
        }
    }
}