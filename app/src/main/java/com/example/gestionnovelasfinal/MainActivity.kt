package com.example.gestionnovelasfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionnovelasfinal.ui.theme.GestionNovelasFinal
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope

class MainActivity : ComponentActivity() {
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }
            var resenas by remember { mutableStateOf<List<Resenas>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val todasNovelas = firestoreRepository.obtenerNovelas()
                val novelasFavoritas = firestoreRepository.obtenerNovelasFavoritas()
                novelas = todasNovelas.map { novela ->
                    novela.copy(isFavorita = novelasFavoritas.any { it.id == novela.id })
                }
            }

            GestionNovelasFinal {
                Navigation(novelas, resenas, { updatedResenas -> resenas = updatedResenas },
                    { updatedNovelas -> novelas = updatedNovelas }, coroutineScope)
            }
        }
    }
}
@Composable
fun Navigation(
    novelas: List<Novela>,
    resenas: List<Resenas>,
    onResenasUpdated: (List<Resenas>) -> Unit,
    onNovelasUpdated: (List<Novela>) -> Unit,
    coroutineScope: CoroutineScope

) {
    val navController = rememberNavController()
    val firestoreRepository = FirestoreRepository()

    NavHost(navController = navController, startDestination = Screen.NovelListScreen.route) {
        composable(Screen.NovelListScreen.route) {
            NovelListScreen(navController, novelas, onNovelasUpdated, firestoreRepository, coroutineScope)
        }
        composable(Screen.AddNovelScreen.route) {
            AddNovelScreen(navController) { nuevaNovela ->
                onNovelasUpdated(novelas + nuevaNovela)
                navController.navigate(Screen.NovelListScreen.route)
            }
        }
        composable(Screen.AddReviewScreen.route) {
                AddReviewScreen(navController) { nuevaResena ->
                  onResenasUpdated(resenas + nuevaResena)
                    navController.navigate(Screen.ReviewListScreen.route)
                }
            }
        composable(Screen.ReviewListScreen.route) {
            ReviewListScreen(navController)

        }
        composable(Screen.FavoriteNovelsScreen.route) {
            FavoriteNovelsScreen(navController, novelas)
        }
    }
}