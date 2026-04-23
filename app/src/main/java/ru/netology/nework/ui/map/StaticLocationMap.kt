package ru.netology.nework.ui.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.netology.nework.model.Coordinates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun StaticLocationMap(
    coordinates: Coordinates,
    modifier: Modifier = Modifier,
) {
    val point = LatLng(coordinates.lat, coordinates.lng)
    val camera = rememberCameraPositionState()
    val mapsAvailable = isMapsConfigured

    LaunchedEffect(point) {
        if (mapsAvailable) {
            camera.move(CameraUpdateFactory.newLatLngZoom(point, 15f))
        }
    }

    if (!mapsAvailable) {
        MapUnavailableCard(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            title = "Карта недоступна",
            message = "Укажи MAPS_API_KEY в gradle.properties, чтобы видеть точки на карте.",
        )
        return
    }

    Card(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cameraPositionState = camera,
        ) {
            AdvancedMarker(
                state = MarkerState(position = point),
                title = "Локация",
            )
        }
    }
}
