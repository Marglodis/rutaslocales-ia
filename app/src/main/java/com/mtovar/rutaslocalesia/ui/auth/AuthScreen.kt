package com.mtovar.rutaslocalesia.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mtovar.rutaslocalesia.R
import com.mtovar.rutaslocalesia.ui.theme.GreenDark
import com.mtovar.rutaslocalesia.ui.theme.GreenLight
import com.mtovar.rutaslocalesia.ui.theme.GreenMedium
import com.mtovar.rutaslocalesia.ui.theme.GreenPrimary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    val message by viewModel.authMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estado para mostrar/ocultar el diálogo de recuperación
    var showForgotDialog by remember { mutableStateOf(false) }

    // CONTENEDOR PRINCIPAL CON DEGRADADO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenLight, Color.White),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // MENSAJE DE ÉXITO (VERDE)
            if (message != null) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = message!!,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // LOGO
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(110.dp),
                tint = GreenPrimary
            )

            // TÍTULO
            Text(
                text = if (isRegisterMode) "Únete a la aventura" else "Bienvenido a Eco",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )

            // Subtítulo
            Text(
                text = "Tu guía de naturaleza inteligente 🌿",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // CAMPO EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = GreenMedium,
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = GreenDark.copy(alpha = 0.6f),
                    cursorColor = GreenPrimary,
                    focusedLeadingIconColor = GreenPrimary,
                    unfocusedLeadingIconColor = GreenMedium
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (passwordVisible) GreenPrimary else GreenMedium
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = GreenMedium,
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = GreenDark.copy(alpha = 0.6f),
                    cursorColor = GreenPrimary,
                    focusedLeadingIconColor = GreenPrimary,
                    unfocusedLeadingIconColor = GreenMedium
                )
            )

            // --- NUEVO: BOTÓN OLVIDÉ CONTRASEÑA (Solo en Login) ---
            if (!isRegisterMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { showForgotDialog = true }) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenDark
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // MENSAJE DE ERROR (ROJO)
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = error!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        // Botón reenviar si es error de verificación
                        if (error!!.contains("verificar", ignoreCase = true)) {
                            TextButton(onClick = { viewModel.resendVerification() }) {
                                Text("Reenviar correo de verificación", fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN PRINCIPAL
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.registerUser(email, password)
                    } else {
                        viewModel.loginUser(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = GreenMedium
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isRegisterMode) "CREAR CUENTA" else "INICIAR SESIÓN",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOGGLE TEXTO (LINK)
            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                colors = ButtonDefaults.textButtonColors(contentColor = GreenDark)
            ) {
                Text(
                    text = if (isRegisterMode) "¿Ya tienes cuenta? Ingresa aquí" else "¿Eres nuevo? Crea una cuenta",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- LÓGICA DEL DIÁLOGO ---
        if (showForgotDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotDialog = false },
                onConfirm = { emailRecuperacion ->
                    viewModel.sendRecoveryEmail(emailRecuperacion)
                    showForgotDialog = false
                }
            )
        }
    }
}

// --- COMPONENTE DEL DIÁLOGO (Al final del archivo) ---
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Recuperar Contraseña", style = MaterialTheme.typography.titleMedium, color = GreenDark) },
        text = {
            Column {
                Text("Ingresa tu correo y te enviaremos un enlace para crear una nueva clave.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(email) },
                enabled = email.isNotBlank()
            ) {
                Text("Enviar", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}