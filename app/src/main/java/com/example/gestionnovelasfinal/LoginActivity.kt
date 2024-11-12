package com.example.gestionnovelasfinal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionnovelasfinal.ui.theme.FixedTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth, onLoginSuccess: (List<Novela>, List<Resenas>, List<String>, Boolean) -> Unit) {
    FixedTheme {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()
        val firestoreRepository = FirestoreRepository()
        val sharedPreferences = SharedPreferences()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                coroutineScope.launch {
                                    try {
                                        val novelas = firestoreRepository.obtenerNovelas()
                                        val resenas = firestoreRepository.obtenerResenas()
                                        val isDarkTheme = sharedPreferences.getUserThemePreference(user.uid)
                                        val favoriteNovels = sharedPreferences.getUserFavoriteNovels(user.uid)
                                        onLoginSuccess(novelas, resenas,favoriteNovels, isDarkTheme)
                                        navController.navigate(Screen.NovelListScreen.route) {
                                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error loading data: ${e.message}"
                                    }
                                }
                            }
                        } else {
                            errorMessage = task.exception?.message
                        }
                    }
                }) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate(Screen.RegisterScreen.route) }) {
                    Text("Don't have an account? Register")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}