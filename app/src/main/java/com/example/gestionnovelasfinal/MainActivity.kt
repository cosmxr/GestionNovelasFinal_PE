package com.example.gestionnovelasfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionnovelasfinal.ui.theme.GestionNovelasFinalTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        sharedPreferences = SharedPreferences()

        setContent {
            var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }
            var resenas by remember { mutableStateOf<List<Resenas>>(emptyList()) }
            var favoriteNovels by remember { mutableStateOf<List<String>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()
            var isDarkTheme by remember { mutableStateOf(false) }

            GestionNovelasFinalTheme(darkTheme = isDarkTheme) {
                Navigation(
                    novelas, resenas, favoriteNovels, { updatedResenas -> resenas = updatedResenas },
                    { updatedNovelas -> novelas = updatedNovelas }, coroutineScope, auth,
                    isDarkTheme, { isDarkTheme = !isDarkTheme
                        auth.currentUser?.let { user -> coroutineScope.launch { sharedPreferences
                            .saveUserThemePreference(user.uid, isDarkTheme) }
                        } }
                ) { loadedNovelas, loadedResenas, loadedFavoriteNovels, loadedIsDarkTheme ->
                    novelas = loadedNovelas
                    resenas = loadedResenas
                    favoriteNovels = loadedFavoriteNovels
                    isDarkTheme = loadedIsDarkTheme
                }
            }
        }
    }
}

@Composable
fun Navigation(
    novelas: List<Novela>,
    resenas: List<Resenas>,
    favoriteNovels: List<String>,
    onResenasUpdated: (List<Resenas>) -> Unit,
    onNovelasUpdated: (List<Novela>) -> Unit,
    coroutineScope: CoroutineScope,
    auth: FirebaseAuth,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLoginSuccess: (List<Novela>, List<Resenas>, List<String>, Boolean) -> Unit
) {
    val navController = rememberNavController()
    val sharedPreferences = SharedPreferences()
    val userId = auth.currentUser?.uid ?: ""

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController, auth, onLoginSuccess)
        }
        composable(Screen.RegisterScreen.route) {
            RegisterScreen(navController, auth)
        }
        composable(Screen.NovelListScreen.route) {
            NovelListScreen(
                navController, novelas, favoriteNovels, onNovelasUpdated,
                FirestoreRepository(), isDarkTheme, coroutineScope,
                onToggleTheme, sharedPreferences, userId
            )
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
            FavoriteNovelsScreen(navController, novelas, onNovelasUpdated,
                isDarkTheme, coroutineScope, onToggleTheme, sharedPreferences, userId)
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(isDarkTheme, onToggleTheme, navController)
        }
    }

}