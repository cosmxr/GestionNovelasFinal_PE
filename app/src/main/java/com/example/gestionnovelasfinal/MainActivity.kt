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
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        sharedPreferences = SharedPreferences()

        // Initialize the MapView
        Configuration.getInstance().userAgentValue = packageName
        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        setContent {
            var novelas by remember { mutableStateOf(listOf<Novela>()) }
            var resenas by remember { mutableStateOf(listOf<Resenas>()) }
            var favoriteNovels by remember { mutableStateOf(listOf<String>()) }
            var isDarkTheme by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            GestionNovelasFinalTheme(darkTheme = isDarkTheme) {
                Navigation(
                    novelas, resenas, favoriteNovels, { updatedResenas -> resenas = updatedResenas },
                    { updatedNovelas -> novelas = updatedNovelas }, coroutineScope, auth, isDarkTheme,
                    { isDarkTheme = !isDarkTheme
                        auth.currentUser?.let { user -> coroutineScope.launch { sharedPreferences
                            .saveUserThemePreference(user.uid, isDarkTheme) }
                        } },
                    { updatedNovelas, updatedResenas, updatedFavoriteNovels, darkTheme ->
                        novelas = updatedNovelas
                        resenas = updatedResenas
                        favoriteNovels = updatedFavoriteNovels
                        isDarkTheme = darkTheme
                    },
                    mapView
                )
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
    onLoginSuccess: (List<Novela>, List<Resenas>, List<String>, Boolean) -> Unit,
    mapView: MapView
) {
    val navController = rememberNavController()
    val sharedPreferences = SharedPreferences()
    val userId = auth.currentUser?.uid ?: ""

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController, auth, onLoginSuccess)
        }
        composable(Screen.MapScreen.route) {
            MapScreen(navController,mapView, novelas)
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
        composable(Screen.AddUbicationScreen.route) {
            val novela = navController.previousBackStackEntry?.savedStateHandle?.get<Novela>("novela")
            if (novela != null) {
                AddUbicationScreen(navController, novela) { updatedNovela ->
                    onNovelasUpdated(novelas.map { if (it.id == updatedNovela.id) updatedNovela else it })
                    navController.navigate(Screen.NovelListScreen.route)
                }
            }
        }
    }
}