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
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.toIntOrNull


sealed class Screen(val route: String) {
    object NovelListScreen : Screen("novel_list")
    object AddNovelScreen : Screen("add_novel")
    object AddReviewScreen : Screen("add_review")
    object ReviewListScreen : Screen("review_list")
    object FavoriteNovelsScreen : Screen("favorite_novels")
}

@Composable
fun NovelListScreen(
    navController: NavController,
    novelas: List<Novela>,
    onNovelasUpdated: (List<Novela>) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Listado de Novelas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate(Screen.AddNovelScreen.route) }) {
                Text("Agregar Novela")
            }
            Button(onClick = { navController.navigate(Screen.ReviewListScreen.route) }) {
                Text("Ver Reseñas")
            }
            Button(onClick = { navController.navigate(Screen.FavoriteNovelsScreen.route) }) {
                Text("Ver Favoritos")
            }
        }

        // Mostrar la lista de novelas
        LazyColumn {
            items(novelas) { novela ->
                TarjetaNovela(
                    novela,
                    onAddReviewClick = { onAddReviewClick(novela, navController) },
                    onToggleFavoriteClick = { toggleFavorite(novela, novelas, onNovelasUpdated) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Funciones auxiliares para el manejo de eventos
private fun onAddReviewClick(novela: Novela, navController: NavController) {
    navController.currentBackStackEntry?.savedStateHandle?.set("novela", novela)
    navController.navigate(Screen.AddReviewScreen.route)
}

// Función auxiliar para marcar o desmarcar una novela como favorita
fun toggleFavorite(
    novela: Novela,
    novelas: List<Novela>,
    onNovelasUpdated: (List<Novela>) -> Unit
) {
    val nuevaLista = novelas.map {
        if (it.id == novela.id) it.copy(isFavorita = !it.isFavorita) else it
    }
    onNovelasUpdated(nuevaLista)
}

// Pantalla para agregar novela
@Composable
fun AddNovelScreen(navController: NavController, onNovelAdded: (Novela) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ano, onValueChange = { ano = it }, label = { Text("Año") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val nuevaNovela = Novela(nombre = titulo, autor = autor, año_publicacion = ano.toIntOrNull() ?: 0, descripcion = descripcion)
            agregarNovelaAFirestore(nuevaNovela) { novelaConId ->
                onNovelAdded(novelaConId)
                navController.navigate(Screen.NovelListScreen.route)
            }
        }) {
            Text("Guardar novela")
        }
    }
}
@Composable
fun AddReviewScreen(navController: NavController, onResenasAdded: (Novela) -> Unit) {
    val novela: Novela? = navController.previousBackStackEntry?.savedStateHandle?.get("novela")
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
            if (novela != null) {
                // Crear una nueva reseña
                val nuevaResena = Resenas(novela.nombre, contenido = textoResena)

                // Agregar la reseña a la lista de reseñas de la novela
                val novelaActualizada = novela.copy(resenas = novela.resenas + nuevaResena)

                // Llamar al callback con la novela actualizada
                onResenasAdded(novelaActualizada)

                // Navegar de vuelta a la lista de novelas
                navController.navigate(Screen.NovelListScreen.route)
            }
        }) {
            Text("Añadir reseña")
        }
    }
}

private fun agregarNovelaAFirestore(novela: Novela, onComplete: (Novela) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("novelas")
        .add(novela)
        .addOnSuccessListener { documentReference ->
            val novelaConId = novela.copy(id = documentReference.id)
            onComplete(novelaConId)
        }
        .addOnFailureListener { exception ->
            println("Error al agregar novela: ${exception.message}")
        }
}

// Pantalla de lista de reseñas
@Composable
fun ReviewListScreen(navController: NavHostController, novelas: List<Novela>) {
    // Recupera las reseñas de la novela seleccionada
    val novela: Novela? = navController.previousBackStackEntry?.savedStateHandle?.get<Novela>("novela")
    val reseñas: List<Resenas> = novela?.resenas ?: emptyList()

    Column {
        LazyColumn {
            items(reseñas) { reseña ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Novela: ${novela?.nombre}", style = MaterialTheme.typography.headlineSmall)
                        Text(text = "Reseña: ${reseña.contenido}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Button(onClick = { navController.navigate(Screen.NovelListScreen.route) { popUpTo(Screen.NovelListScreen.route) { inclusive = true } } },
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Volver")
        }
    }
}

@Composable
fun FavoriteNovelsScreen(
    navController: NavHostController,
    novelas: List<Novela>
) {
    val favoriteNovels = novelas.filter { it.isFavorita }

    Column {
        LazyColumn {
            items(favoriteNovels) { novela ->
                TarjetaNovela(
                    novela,
                    onAddReviewClick = {}, // No necesitamos manejar esto aquí
                    onToggleFavoriteClick = {} // No necesitamos manejar esto aquí
                )
            }
        }
        Button(onClick = {
            navController.navigate(Screen.NovelListScreen.route) {
                popUpTo(Screen.NovelListScreen.route) { inclusive = true }
            }
        },
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Volver")
        }
    }
}
@Composable
fun TarjetaNovela(
    novela: Novela,
    onAddReviewClick: (Novela) -> Unit,
    onToggleFavoriteClick: (Novela) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(novela.nombre, style = MaterialTheme.typography.headlineSmall)
            Text("Por ${novela.autor}, ${novela.año_publicacion}")
            Text(novela.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { onAddReviewClick(novela) }) {
                    Text("Añadir reseña")
                }

                // Icono de corazón para marcar o desmarcar como favorita
                IconButton(onClick = { onToggleFavoriteClick(novela) }) {
                    if (novela.isFavorita) {
                        // Corazón lleno si es favorita
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Desmarcar como favorita",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        // Corazón vacío si no es favorita
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Marcar como favorita",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
