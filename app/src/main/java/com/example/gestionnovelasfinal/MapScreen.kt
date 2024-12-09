package com.example.gestionnovelasfinal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, mapView: MapView, novelas: List<Novela>) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior con botón de retroceso
        TopAppBar(
            title = { Text(text = "Mapa de Novelas") },
            navigationIcon = {
                IconButton(onClick = { navController.navigate(Screen.NovelListScreen.route) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        // Vista del mapa
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { map ->
            map.overlays.clear()
            novelas.forEach { novela ->
                val marker = Marker(map)
                marker.position = GeoPoint(novela.latitude ?: 0.0, novela.longitude ?: 0.0)
                marker.title = novela.nombre
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)
            }

            if (novelas.isNotEmpty()) {
                val firstNovela = novelas.first()
                map.controller.setZoom(12.0)
                map.controller.setCenter(GeoPoint(firstNovela.latitude ?: 0.0, firstNovela.longitude ?: 0.0))
            } else {
                map.controller.setZoom(5.0)
                map.controller.setCenter(GeoPoint(0.0, 0.0))
            }
        }
    }
}


