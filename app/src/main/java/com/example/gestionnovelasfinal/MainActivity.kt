package com.example.gestionnovelasfinal


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionnovelasfinal.Screen.AddNovelScreen.NovelListScreen
import com.example.gestionnovelasfinal.ui.theme.GestionNovelasFinal


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestionNovelasFinal {
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }
    val onFavoriteClick: (Novela) -> Unit = { novela ->
        novelas = novelas.map { n ->
            if (n == novela) {
                n.copy(isFavorita = !n.isFavorita)
            } else {
                n
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.NovelListScreen.route) {
        composable(Screen.NovelListScreen.route) {
            NovelListScreen(navController, novelas, onNovelasUpdated = { novelas = it })
        }
        composable(Screen.AddNovelScreen.route) {
            AddNovelScreen(navController) { nuevaNovela ->
                novelas = novelas + nuevaNovela
                navController.navigate(Screen.NovelListScreen.route)
            }
        }
        composable(Screen.AddReviewScreen.route) {
            val novela = navController.previousBackStackEntry?.savedStateHandle?.get<Novela>("novela")
            novela?.let {
                AddReviewScreen(navController, it) { nuevaResena ->
                    novelas = novelas.map { n ->
                        if (n == novela) {
                            n.copy(resenas = (n.resenas + nuevaResena).toMutableList())
                        } else {
                            n
                        }
                    }
                    navController.navigate(Screen.NovelListScreen.route)
                }
            }
        }
        composable(Screen.ReviewListScreen.route) { ReviewListScreen(navController, novelas) }
        composable(Screen.FavoriteNovelsScreen.route) { FavoriteNovelsScreen(navController, novelas) }
    }
}