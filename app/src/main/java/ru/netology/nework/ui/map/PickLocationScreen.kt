package ru.netology.nework.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ru.netology.nework.util.toCoordinatesOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickLocationScreen(
    navController: NavController,
    resultKey: String,
    initialLocation: String = "",
) {
    val defaultPoint = LatLng(55.751244, 37.618423)
    val initialPoint = initialLocation.toCoordinatesOrNull()?.let { coordinates ->
        LatLng(coordinates.lat, coordinates.lng)
    } ?: defaultPoint
    var selectedPoint by rememberSaveable(stateSaver = LatLngSaver) {
        mutableStateOf(initialPoint)
    }
    val cameraPositionState = rememberCameraPositionState()
    val mapsAvailable = isMapsConfigured

    LaunchedEffect(selectedPoint) {
        if (mapsAvailable) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(selectedPoint, 14f)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор локации") },
            )
        },
        floatingActionButton = {
            if (mapsAvailable) {
                FloatingActionButton(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(resultKey, "${selectedPoint.latitude}, ${selectedPoint.longitude}")
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (mapsAvailable) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    onMapClick = { point ->
                        selectedPoint = point
                    },
                ) {
                    AdvancedMarker(
                        state = MarkerState(position = selectedPoint),
                        title = "Выбранная точка",
                    )
                }
            } else {
                MapUnavailableCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = "Карта недоступна",
                    message = "Добавь MAPS_API_KEY в gradle.properties, чтобы выбирать локацию без падения экрана.",
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (mapsAvailable) {
                        "Нажми на карту, чтобы выбрать точку"
                    } else {
                        "Сейчас можно вернуться назад и продолжить без локации."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        if (mapsAvailable) {
                            selectedPoint = defaultPoint
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(defaultPoint, 14f)
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Text(
                        text = if (mapsAvailable) " Сбросить к центру" else " Вернуться",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private val LatLngSaver: Saver<LatLng, List<Double>> = Saver(
    save = { point -> listOf(point.latitude, point.longitude) },
    restore = { values ->
        if (values.size == 2) {
            LatLng(values[0], values[1])
        } else {
            null
        }
    },
)
