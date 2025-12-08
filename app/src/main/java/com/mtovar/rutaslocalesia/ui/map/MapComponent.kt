package com.mtovar.rutaslocalesia.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mtovar.rutaslocalesia.model.Ruta

@Composable
fun MapViewContainer(rutas: List<Ruta>,
                     onMarkerClick: (Ruta) -> Unit,
                     modifier: Modifier = Modifier,
                     liteMode: Boolean = false
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
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // ESTA ES LA CLAVE PARA EL RENDIMIENTO:
        googleMapOptionsFactory = {
            GoogleMapOptions().liteMode(liteMode)
        },
        properties = MapProperties(
            isMyLocationEnabled = false // Ponlo en 'true' SOLO si ya pediste permisos.
            // Si lo pones en true sin permisos, la app crasheará.
            // Para este sprint, dejémoslo en false o implementa un PermissionLauncher.
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false, // Botón para centrar en mi
            zoomControlsEnabled = false
        )
    ) {
        rutas.forEach { ruta ->
            // 1. MARCADOR PRINCIPAL (ROJO)
            Marker(
                state = MarkerState(position = LatLng(ruta.latitud, ruta.longitud)),
                title = "📍 ${ruta.nombre}",
                snippet = ruta.duracion,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = {
                    onMarkerClick(ruta)
                    false
                }
            )

            // 2. PUNTOS DE INTERÉS (AZULES/CYAN)
            ruta.puntosInteres.forEach { poi ->
                Marker(
                    state = MarkerState(position = LatLng(poi.latitud, poi.longitud)),
                    title = poi.nombre, // Ej: "Mirador"
                    snippet = poi.tipo.uppercase(),
                    // Usamos un color diferente para distinguirlos
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }
    }
}