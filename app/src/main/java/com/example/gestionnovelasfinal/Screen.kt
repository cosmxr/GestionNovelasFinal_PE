package com.example.gestionnovelasfinal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.text.toIntOrNull

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object RegisterScreen : Screen("register")
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
    onNovelasUpdated: (List<Novela>) -> Unit,
    firestoreRepository: FirestoreRepository,
    coroutineScope: CoroutineScope
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

        LazyColumn {
            items(novelas) { novela ->
                TarjetaNovela(
                    novela,
                    onAddReviewClick = { onAddReviewClick(novela, navController) },
                    onToggleFavoriteClick = { toggleFavorite(novela, novelas, onNovelasUpdated, firestoreRepository, coroutineScope) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun onAddReviewClick(novela: Novela, navController: NavController) {
    navController.currentBackStackEntry?.savedStateHandle?.set("novela", novela)
    navController.navigate(Screen.AddReviewScreen.route)
}

fun toggleFavorite(
    novela: Novela,
    novelas: List<Novela>,
    onNovelasUpdated: (List<Novela>) -> Unit,
    firestoreRepository: FirestoreRepository,
    coroutineScope: CoroutineScope
) {
    val nuevaLista = novelas.map {
        if (it.id == novela.id) it.copy(isFavorita = !it.isFavorita) else it
    }
    onNovelasUpdated(nuevaLista)

    coroutineScope.launch {
        firestoreRepository.agregarNovelasFavoritas(novela.id, !novela.isFavorita)
    }
}

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
fun AddReviewScreen(navController: NavController, onResenasAdded: (Resenas) -> Unit) {
    val novela: Novela? = navController.previousBackStackEntry?.savedStateHandle?.get("novela")
    var textoResena by remember { mutableStateOf("") }
    val firestoreRepository = FirestoreRepository()
    val coroutineScope = rememberCoroutineScope()

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
                val nuevaResena = Resenas(novela.id,novela.nombre, contenido = textoResena)
                onResenasAdded(nuevaResena)
                coroutineScope.launch {
                    firestoreRepository.agregarResena(novela.id, novela.nombre,nuevaResena)
                }
                navController.navigate(Screen.NovelListScreen.route)
            }
        }) {
            Text("Añadir reseña")
        }
    }
}

private fun agregarNovelaAFirestore(novela: Novela, onComplete: (Novela) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("novelasClasicas")
        .add(novela)
        .addOnSuccessListener { documentReference ->
            val novelaConId = novela.copy(id = documentReference.id)
            onComplete(novelaConId)
        }
        .addOnFailureListener { exception ->
            println("Error al agregar novela: ${exception.message}")
        }
}

@Composable
fun ReviewListScreen(navController: NavHostController) {
    val firestoreRepository = FirestoreRepository()
    val coroutineScope = rememberCoroutineScope()
    val resenasList = remember { mutableStateListOf<Resenas>() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val resenasFirestore = firestoreRepository.obtenerResenas()
                resenasList.clear()
                resenasList.addAll(resenasFirestore)
            } catch (e: Exception) {
                println("Error al obtener reseñas: ${e.message}")
            }
        }
    }

    Column {
        LazyColumn {
            items(resenasList) { resena ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(resena.nombre, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "Reseña: ${resena.contenido}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row {
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        firestoreRepository.eliminarResena(resena)
                                        resenasList.remove(resena)
                                    } catch (e: Exception) {
                                        println("Error al eliminar reseña: ${e.message}")
                                    }
                                }
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }


            }
        }
        Button(
            onClick = {
                navController.navigate(Screen.NovelListScreen.route) {
                    popUpTo(
                        Screen.NovelListScreen.route
                    ) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Volver")
        }
    }}
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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

                IconButton(onClick = { onToggleFavoriteClick(novela) }) {
                    if (novela.isFavorita) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Desmarcar como favorita",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
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