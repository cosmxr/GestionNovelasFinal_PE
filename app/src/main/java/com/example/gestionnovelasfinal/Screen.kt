package com.example.gestionnovelasfinal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlin.text.toIntOrNull


sealed class Screen(val route: String) {
    object NovelListScreen : Screen("novel_list")
    object AddNovelScreen : Screen("add_novel")
    object AddReviewScreen : Screen("add_review")
    object ReviewListScreen : Screen("review_list")
    object FavoriteNovelsScreen : Screen("favorite_novels")

    @Composable
    fun NovelListScreen(navController: NavHostController, novelas: List<Novela>, onNovelasUpdated: (List<Novela>) -> Unit) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (novelas.isEmpty()) {
                Text(
                    text = "Bienvenido usuari@\nAgregue una novela para empezar con su lista.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ListaNovelas(novelas,
                onAddReviewClick = { novela ->navController.currentBackStackEntry?.savedStateHandle?.set("novela", novela)
                    navController.navigate(Screen.AddReviewScreen.route)
                },
                onFavoriteClick = { novela ->
                    onNovelasUpdated(novelas.map { n ->
                        if (n == novela) {
                            n.copy(isFavorita = !n.isFavorita)
                        } else {
                            n
                        }
                    })
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ver la lista de reseñas
            if (novelas.isNotEmpty()) { //solo se mostrara cuando la lista de novelas no este vacia
                Button(onClick = {
                    navController.navigate(Screen.ReviewListScreen.route) {
                        popUpTo(Screen.NovelListScreen.route) { inclusive = true }
                    }
                }) {
                    Text("Ver reseñas")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Botón para ver favoritos
                Button(onClick = { navController.navigate(Screen.FavoriteNovelsScreen.route) }) {
                    Text("Ver favoritos")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para añadir novela (siempre visible)
            Button(onClick = { navController.navigate(Screen.AddNovelScreen.route) }) {
                Text("Añadir novela")
            }


        }
    }

}
@Composable
fun AddNovelScreen(navController: NavController, onNovelAdded: (Novela) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = autor,
            onValueChange = { autor = it },
            label = { Text("Autor") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ano,
            onValueChange = { ano = it },
            label = { Text("Año") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val nuevaNovela = Novela(titulo, autor, ano.toIntOrNull() ?: 0, descripcion)
            onNovelAdded(nuevaNovela)
        }) {
            Text("Guardar novela")
        }
    }
}

@Composable
fun AddReviewScreen(navController: NavController, novela: Novela, onReviewAdded: (Resenas) -> Unit) {
    var textoResena by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = textoResena,
            onValueChange = {
                if (it.length <= 200) {
                    textoResena = it
                }
            },
            label = { Text("Escribe tu reseña (máx. 200 caracteres)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val nuevaResena = Resenas(textoResena)
            onReviewAdded(nuevaResena)
        }) {
            Text("Añadir reseña")
        }
    }
}
//
@Composable
fun ReviewListScreen(navController: NavHostController, novelas: List<Novela>) {
    Column { // Se agrega un Column para organizar el contenido
        LazyColumn {
            items(novelas) { novela ->
                novela.resenas.forEach { resena ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "Novela: ${novela.titulo}", style = MaterialTheme.typography.headlineSmall)
                            Text(text = "Reseña: ${resena.contenido}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        // Botón Volver
        Button(
            onClick = { navController.navigate(Screen.NovelListScreen.route) { popUpTo(Screen.NovelListScreen.route)
            { inclusive = true}
            }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun ListaNovelas(novelas: List<Novela>, onAddReviewClick: (Novela) -> Unit, onFavoriteClick: (Novela) -> Unit) {
    LazyColumn {
        items(novelas) { novela ->
            TarjetaNovela(novela, onAddReviewClick, onFavoriteClick)
        }
    }
}

@Composable
fun TarjetaNovela(novela: Novela, onAddReviewClick: (Novela) -> Unit, onFavoriteClick: (Novela) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(novela.titulo, style = MaterialTheme.typography.headlineSmall)
            Text("Por ${novela.autor}, ${novela.ano}")
            Text(novela.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { onAddReviewClick(novela) }) {
                    Text("Añadir reseña")
                }
                IconButton(onClick = { onFavoriteClick(novela) }) {
                    Icon(
                        imageVector = if (novela.isFavorita) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (novela.isFavorita) "Eliminar de favoritos" else "Añadir a favoritos"
                    )
                }
            }
        }
    }
}
@Composable
fun FavoriteNovelsScreen(navController: NavHostController, novelas: List<Novela>) {
    val favoriteNovels = novelas.filter { it.isFavorita }

    Column {
        LazyColumn {
            items(favoriteNovels) { novela ->
                TarjetaNovela(novela, {}, {}) // No se necesitan los clicks aquí
            }
        }
        // Botón Volver
        Button(
            onClick = {
                navController.navigate(Screen.NovelListScreen.route) {
                    popUpTo(Screen.NovelListScreen.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Volver")
        }
    }
}
