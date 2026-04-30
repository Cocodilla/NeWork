package ru.netology.nework.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import ru.netology.nework.R
import ru.netology.nework.util.toCoordinatesOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickLocationScreen(
    navController: NavController,
    resultKey: String,
    initialLocation: String = "",
) {
    val context = LocalContext.current
    val defaultPoint = Point(55.751244, 37.618423)
    val initialPoint = initialLocation.toCoordinatesOrNull()?.let { coordinates ->
        Point(coordinates.lat, coordinates.lng)
    } ?: defaultPoint
    var selectedPoint by rememberSaveable(stateSaver = PointSaver) {
        mutableStateOf(initialPoint)
    }
    val mapsAvailable = isMapsConfigured

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_pick_location)) },
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
                val mapView = rememberMapViewWithLifecycle()
                val mapTapListener = remember {
                    object : InputListener {
                        override fun onMapTap(
                            map: com.yandex.mapkit.map.Map,
                            point: Point,
                        ) {
                            selectedPoint = point
                        }

                        override fun onMapLongTap(
                            map: com.yandex.mapkit.map.Map,
                            point: Point,
                        ) {
                            selectedPoint = point
                        }
                    }
                }

                DisposableEffect(mapView, mapTapListener) {
                    mapView.mapWindow.map.addInputListener(mapTapListener)
                    onDispose {
                        mapView.mapWindow.map.removeInputListener(mapTapListener)
                    }
                }

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        val map = view.mapWindow.map
                        map.move(
                            CameraPosition(selectedPoint, 14f, 0f, 0f),
                            Animation(Animation.Type.SMOOTH, 0.25f),
                            null,
                        )
                        map.mapObjects.clear()
                        map.mapObjects.addPlacemark().apply {
                            geometry = selectedPoint
                            setIcon(context.mapMarkerIcon())
                        }
                    },
                )
            } else {
                MapUnavailableCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = stringResource(R.string.map_unavailable_title),
                    message = stringResource(R.string.map_unavailable_pick_message),
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
                        stringResource(R.string.map_hint_pick_point)
                    } else {
                        stringResource(R.string.map_hint_continue_without_location)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        if (mapsAvailable) {
                            selectedPoint = defaultPoint
                        } else {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Text(
                        text = if (mapsAvailable) {
                            " ${stringResource(R.string.map_reset_to_center)}"
                        } else {
                            " ${stringResource(R.string.action_return)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private val PointSaver: Saver<Point, List<Double>> = Saver(
    save = { point -> listOf(point.latitude, point.longitude) },
    restore = { values ->
        if (values.size == 2) {
            Point(values[0], values[1])
        } else {
            null
        }
    },
)
