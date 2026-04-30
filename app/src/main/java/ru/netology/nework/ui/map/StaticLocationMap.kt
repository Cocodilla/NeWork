package ru.netology.nework.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.netology.nework.R
import ru.netology.nework.model.Coordinates

@Composable
fun StaticLocationMap(
    coordinates: Coordinates,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val staticMapUrl = remember(coordinates) { coordinates.toStaticMapUrl() }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SubcomposeAsyncImage(
                model = staticMapUrl,
                contentDescription = stringResource(R.string.event_location_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { openCoordinatesInMaps(context, coordinates) },
                contentScale = ContentScale.Crop,
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    is AsyncImagePainter.State.Error -> {
                        MapUnavailableCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            title = stringResource(R.string.map_unavailable_title),
                            message = stringResource(R.string.map_unavailable_static_message),
                            coordinates = coordinates,
                        )
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.map_loading),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            Button(
                onClick = { openCoordinatesInMaps(context, coordinates) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                Text(text = stringResource(R.string.action_open_in_maps))
            }
        }
    }
}
