package com.example.gestionnovelasfinal

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object RegisterScreen : Screen("register")
    object NovelListScreen : Screen("novel_list")
    object AddNovelScreen : Screen("add_novel")
    object AddReviewScreen : Screen("add_review")
    object ReviewListScreen : Screen("review_list")
    object SettingsScreen : Screen("settings")
    object FavoriteNovelsScreen : Screen("favorite_novels")
    object MapScreen : Screen("map_activity")
    object AddUbicationScreen : Screen("add_ubication")
}

@Composable
fun NovelListScreen(
    navController: NavController,
    novelas: List<Novela>,
    favoriteNovels: List<String>,
    onNovelasUpdated: (List<Novela>) -> Unit,
    firestoreRepository: FirestoreRepository,
    isDarkTheme: Boolean,
    coroutineScope: CoroutineScope,
    onToggleTheme: () -> Unit,
    sharedPreferences: SharedPreferences,
    userId: String
) {
    val resources = LocalContext.current.resources
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            BitmapUtils.recycleBitmap(bitmap)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Listado de Novelas", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Toggle Theme"
                    )
                }
                IconButton(onClick = {
                    val novelasWithLocation = novelas.filter { it.latitude != null && it.longitude != null }
                    navController.currentBackStackEntry?.savedStateHandle?.set("novelas", ArrayList(novelasWithLocation))
                    navController.navigate(Screen.MapScreen.route)
                }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Show Map"
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { navController.navigate(Screen.AddNovelScreen.route) }) {
                    Text("Agregar Novela")
                }
                Spacer(modifier = Modifier.width(2.dp))
                Button(onClick = { navController.navigate(Screen.ReviewListScreen.route) }) {
                    Text("Ver Reseñas")
                }
                Spacer(modifier = Modifier.width(2.dp))
                Button(onClick = { navController.navigate(Screen.FavoriteNovelsScreen.route) }) {
                    Text("Favoritos")
                }
            }

            LazyColumn {
                items(novelas) { novela ->
                    TarjetaNovela(
                        novela,
                        favoriteNovels,
                        onAddReviewClick = { onAddReviewClick(novela, navController) },
                        onToggleFavoriteClick = { toggleFavorite(novela, novelas, onNovelasUpdated,
                            coroutineScope, sharedPreferences, userId) },
                        onAddUbicationClick = { onAddUbicationClick(novela, navController) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun onAddReviewClick(novela: Novela, navController: NavController) {
    navController.currentBackStackEntry?.savedStateHandle?.set("novela", novela)
    navController.navigate(Screen.AddReviewScreen.route)
}



@Composable
fun AddNovelScreen(navController: NavController, onNovelAdded: (Novela) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier =Modifier.paddingFromBaseline(top = 100.dp))
            OutlinedTextField(value = titulo, onValueChange = { titulo = it },
                label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = autor, onValueChange = { autor = it },
                label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ano, onValueChange = { ano = it },
                label = { Text("Año") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val nuevaNovela = Novela(nombre = titulo, autor = autor, año_publicacion = ano.toIntOrNull() ?: 0, descripcion = descripcion)
                agregarNovelaAFirestore(nuevaNovela) { novelaConId ->
                    onNovelAdded(novelaConId)
                    navController.navigate(Screen.NovelListScreen.route)
                }
            }) {
                Text("Guardar novela")
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
        }
    }}

@Composable
fun AddReviewScreen(navController: NavController, onResenasAdded: (Resenas) -> Unit) {
    val novela: Novela? = navController.previousBackStackEntry?.savedStateHandle?.get("novela")
    var textoResena by remember { mutableStateOf("") }
    val firestoreRepository = FirestoreRepository()
    val coroutineScope = rememberCoroutineScope()
    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .paddingFromBaseline(top = 200.dp)) {

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
        }
    }}

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
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){
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

        Column (modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .paddingFromBaseline(top = 130.dp)) {

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
        }}}

fun toggleFavorite(
    novela: Novela,
    novelas: List<Novela>,
    onNovelasUpdated: (List<Novela>) -> Unit,
    coroutineScope: CoroutineScope,
    sharedPreferences: SharedPreferences,
    userId: String
) {
    coroutineScope.launch {
        // Copiamos la lista actual de novelas para hacer una versión modificada
        val updatedNovelas = novelas.map {
            // Cambiamos el estado de la novela que coincide
            if (it.id == novela.id) it.copy(isFavorita = !novela.isFavorita) else it
        }

        // Llamamos a la función de callback con la lista actualizada
        onNovelasUpdated(updatedNovelas)

        // Guardamos la nueva lista de favoritos en las preferencias compartidas
        val favoriteNovels = sharedPreferences.getUserFavoriteNovels(userId).toMutableList()
        if (novela.isFavorita) {
            favoriteNovels.remove(novela.id)
        } else {
            favoriteNovels.add(novela.id)
        }
        sharedPreferences.saveUserFavoriteNovels(userId, favoriteNovels)
    }
}


@Composable
fun FavoriteNovelsScreen(
    navController: NavController,
    novelas: List<Novela>,
    onNovelasUpdated: (List<Novela>) -> Unit,
    isDarkTheme: Boolean,
    coroutineScope: CoroutineScope,
    onToggleTheme: () -> Unit,
    sharedPreferences: SharedPreferences,
    userId: String
) {
    val favoriteNovels = remember { mutableStateListOf<Novela>() }
    LaunchedEffect(userId) {
        coroutineScope.launch {
            val favoriteNovelsIds = sharedPreferences.getUserFavoriteNovels(userId)
            val userFavoriteNovels = novelas.filter { it.id in favoriteNovelsIds }
            favoriteNovels.clear()
            favoriteNovels.addAll(userFavoriteNovels)
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Text(text = "Novelas Favoritas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(46.dp))
        LazyColumn {
            items(favoriteNovels) { novela ->
                TarjetaNovela(
                    novela,
                    favoriteNovels.map { it.id },
                    onAddReviewClick = { onAddReviewClick(novela, navController) },
                    onToggleFavoriteClick = { toggleFavorite(novela, novelas, onNovelasUpdated, coroutineScope, sharedPreferences, userId) },
                    onAddUbicationClick = { onAddUbicationClick(novela, navController) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate(Screen.NovelListScreen.route) {
                popUpTo(Screen.NovelListScreen.route) { inclusive = true }
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Volver")
        }
    }
}
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    navController: NavController
) {
    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.padding(66.dp)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 26.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dark Theme")
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleTheme() }
                        )
                    }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Button(onClick = {
                            navController.navigate(Screen.NovelListScreen.route) {
                                popUpTo(Screen.NovelListScreen.route) { inclusive = true }
                            }
                        }) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }}

private fun onAddUbicationClick(novela: Novela, navController: NavController) {
    navController.currentBackStackEntry?.savedStateHandle?.set("novela", novela)
    navController.navigate(Screen.AddUbicationScreen.route)
}

@Composable
fun AddUbicationScreen(navController: NavController, novela: Novela, onUbicationAdded: (Novela) -> Unit) {
    var selectedCity by remember { mutableStateOf("") }
    val cities = listOf("Zaragoza", "Madrid", "Barcelona", "Valencia", "Sevilla","Granada","Paris",
        "London","New York","Tokyo","Sydney","Berlin","Rome","Buenos Aires")
    val cityCoordinates = mapOf(
        "Zaragoza" to GeoPoint(41.648823, -0.889085),
        "Madrid" to GeoPoint(40.416775, -3.703790),
        "Barcelona" to GeoPoint(41.385064, 2.173404),
        "Valencia" to GeoPoint(39.469907, -0.376288),
        "Sevilla" to GeoPoint(37.389092, -5.984459),
        "Granada" to GeoPoint(37.177336, -3.598557),
        "Paris" to GeoPoint(48.856614, 2.352222),
        "London" to GeoPoint(51.507351, -0.127758),
        "New York" to GeoPoint(40.712776, -74.005974),
        "Tokyo" to GeoPoint(35.689487, 139.691711),
        "Sydney" to GeoPoint(-33.868820, 151.209290),
        "Berlin" to GeoPoint(52.520008, 13.404954)
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 86.dp)
        .padding(16.dp))
        {
        Text(text = "Selecciona una ciudad", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        cities.forEach { city ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = selectedCity == city,
                    onClick = { selectedCity = city }
                )
                Text(text = city, modifier = Modifier.padding(start = 5.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(onClick = {
            val coordinates = cityCoordinates[selectedCity]
            if (coordinates != null) {
                val updatedNovela = novela.copy(latitude = coordinates.latitude, longitude = coordinates.longitude)
                coroutineScope.launch {
                    FirestoreRepository().actualizarNovela(updatedNovela)
                    onUbicationAdded(updatedNovela)
                    navController.navigate(Screen.NovelListScreen.route) {
                        popUpTo(Screen.NovelListScreen.route) { inclusive = true }
                    }
                }
            }
        }) {
            Text("Guardar Ubicación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(Screen.NovelListScreen.route) {
                popUpTo(Screen.NovelListScreen.route) { inclusive = true }
            }
        }) {
            Text("Volver")
        }
    }
}

@Composable
fun TarjetaNovela(
    novela: Novela,
    favoriteNovels: List<String>,
    onAddReviewClick: (Novela) -> Unit,
    onToggleFavoriteClick: (Novela) -> Unit,
    onAddUbicationClick: (Novela) -> Unit

) {
    var isFavorita by remember { mutableStateOf(favoriteNovels.contains(novela.id)) }
    val resources = LocalContext.current.resources
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            BitmapUtils.recycleBitmap(bitmap)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = novela.nombre, style = MaterialTheme.typography.headlineSmall)
            Text(text = novela.descripcion, style = MaterialTheme.typography.bodyMedium)

            bitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { onAddReviewClick(novela) }) {
                    Text("Añadir Reseña")
                }
                Button(onClick = { onAddUbicationClick(novela) }) {
                    Text("Añadir Ubicación")
                }

                IconButton(
                    onClick = {
                        isFavorita = !isFavorita
                        onToggleFavoriteClick(novela)
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorita) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorita) "Eliminar de favoritos" else "Agregar a favoritos"
                    )
                }
            }
        }
    }
}