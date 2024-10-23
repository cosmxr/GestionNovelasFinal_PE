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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)

        setContent {
            var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }
            var resenas by remember { mutableStateOf<List<Resenas>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()
            val firestoreRepository = FirestoreRepository()

            LaunchedEffect(auth.currentUser) {
                if (auth.currentUser != null) {
                    val todasNovelas = firestoreRepository.obtenerNovelas()
                    novelas = todasNovelas
                }
            }

            GestionNovelasFinal {
                Navigation(novelas, resenas, { updatedResenas -> resenas = updatedResenas },
                    { updatedNovelas -> novelas = updatedNovelas }, coroutineScope, auth)
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
    coroutineScope: CoroutineScope,
    auth: FirebaseAuth
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController, auth)
        }
        composable(Screen.RegisterScreen.route) {
            RegisterScreen(navController, auth)
        }
        composable(Screen.NovelListScreen.route) {
            NovelListScreen(navController, novelas, onNovelasUpdated, FirestoreRepository(), coroutineScope)
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