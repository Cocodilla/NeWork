package ru.netology.nework.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.netology.nework.BuildConfig
import ru.netology.nework.R
import ru.netology.nework.model.Coordinates

internal val isMapsConfigured: Boolean
    get() = BuildConfig.YANDEX_MAPKIT_API_KEY.isNotBlank()

internal fun Coordinates.toStaticMapUrl(): String =
    Uri.Builder()
        .scheme("https")
        .authority("static-maps.yandex.ru")
        .appendEncodedPath("1.x")
        .appendQueryParameter("lang", "ru_RU")
        .appendQueryParameter("l", "map")
        .appendQueryParameter("z", "15")
        .appendQueryParameter("size", "650,360")
        .appendQueryParameter("ll", "$lng,$lat")
        .appendQueryParameter("pt", "$lng,$lat,pm2rdm")
        .build()
        .toString()

private fun Coordinates.toBrowserMapUrl(): String =
    Uri.Builder()
        .scheme("https")
        .authority("yandex.ru")
        .appendPath("maps")
        .appendQueryParameter("ll", "$lng,$lat")
        .appendQueryParameter("z", "15")
        .appendQueryParameter("pt", "$lng,$lat,pm2rdm")
        .build()
        .toString()

internal fun openCoordinatesInMaps(context: Context, coordinates: Coordinates) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(coordinates.toBrowserMapUrl()),
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

@Composable
internal fun MapUnavailableCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    coordinates: Coordinates? = null,
) {
    val context = LocalContext.current

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            coordinates?.let { point ->
                Button(
                    onClick = {
                        openCoordinatesInMaps(context, point)
                    }
                ) {
                    Text(text = context.getString(R.string.action_open_in_maps))
                }
            }
        }
    }
}
