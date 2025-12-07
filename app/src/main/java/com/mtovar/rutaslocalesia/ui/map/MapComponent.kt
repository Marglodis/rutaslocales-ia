package com.mtovar.rutaslocalesia.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mtovar.rutaslocalesia.model.Ruta

@Composable
fun MapViewContainer(rutas: List<Ruta>,
                     onMarkerClick: (Ruta) -> Unit
                     ) {
    // Si no hay rutas, centramos en un punto default (ej: Santiago de Chile)
    // En una app real, se usaría la ubicación del usuario.
    val startLocation = if (rutas.isNotEmpty()) {
        LatLng(rutas[0].latitud, rutas[0].longitud)
    } else {
        LatLng(-33.4489, -70.6693)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 12f)
    }

    // Si cambia la lista de rutas, movemos la cámara a la primera ruta
    LaunchedEffect(rutas) {
        if (rutas.isNotEmpty()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(rutas[0].latitud, rutas[0].longitud), 13f
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        rutas.forEach { ruta ->
            /*Marker(
                state = MarkerState(position = LatLng(ruta.latitud, ruta.longitud)),
                title = ruta.nombre,
                snippet = ruta.descripcion
            )*/
            Marker(
                state = MarkerState(position = LatLng(ruta.latitud, ruta.longitud)),
                title = ruta.nombre,
                // Al hacer clic, notificamos al padre y devolvemos true para consumir el evento
                onClick = {
                    onMarkerClick(ruta)
                    true
                }
            )
        }
    }
}