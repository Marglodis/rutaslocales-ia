package com.mtovar.rutaslocalesia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mtovar.rutaslocalesia.ui.chat.ChatScreen
import com.mtovar.rutaslocalesia.ui.theme.RutaslocalesiaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configuramos el SplashScreen
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RutaslocalesiaTheme {
                ChatScreen()
            }
        }
    }
}