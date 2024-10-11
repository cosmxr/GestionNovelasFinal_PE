package com.example.gestionnovelasfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionnovelasfinal.Screen.AddNovelScreen
import com.example.gestionnovelasfinal.ui.theme.GestionNovelasFinal
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            // Usar un estado mutable para novelas
            var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }

            // Recuperar las novelas desde Firestore al iniciar
            LaunchedEffect(Unit) {
                novelas = firestoreRepository.obtenerNovelas()
            }

            GestionNovelasFinal {
                Navigation(novelas, { updatedNovelas -> novelas = updatedNovelas }) // Manejar actualización de novelas
            }
        }
    }
}

@Composable
fun Navigation(novelas: List<Novela>, onNovelasUpdated: (List<Novela>) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.NovelListScreen.route) {
        composable(Screen.NovelListScreen.route) {
            NovelListScreen(navController, novelas, onNovelasUpdated) // Pasar el manejador de actualización
        }
        composable(Screen.AddNovelScreen.route) {
            AddNovelScreen(navController) { nuevaNovela ->
                // Agregar nueva novela a la lista
                onNovelasUpdated(novelas + nuevaNovela) // Actualiza novelas usando el manejador
                navController.navigate(Screen.NovelListScreen.route)
            }
        }
        composable(Screen.AddReviewScreen.route) {
            val novela = navController.previousBackStackEntry?.savedStateHandle?.get<Novela>("novela")
            novela?.let {
                AddReviewScreen(navController) { nuevaResena ->
                    // Buscar la novela en la lista de novelas y agregarle la nueva reseña
                    val novelaActualizada = novela.copy(resenas = (novela.resenas + nuevaResena) as List<Resenas>)

                    // Actualizar la lista de novelas con la novela actualizada
                    val nuevasNovelas = novelas.map { if (it.id == novelaActualizada.id) novelaActualizada else it }
                    onNovelasUpdated(nuevasNovelas)
                }
            }
        }
        composable(Screen.ReviewListScreen.route) {
            ReviewListScreen(navController, novelas)
        }
        composable(Screen.FavoriteNovelsScreen.route) {
            FavoriteNovelsScreen(navController, novelas)
        }
    }
}
