package ru.netology.nework.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable

@Composable
fun ExternalLinkText(
    url: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val context = LocalContext.current

    Text(
        text = url,
        modifier = modifier.clickable { context.openExternalLink(url) },
        color = color,
        textDecoration = TextDecoration.Underline,
    )
}

private fun Context.openExternalLink(url: String) {
    val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "https://$url"
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        startActivity(intent)
    }.onFailure {
        if (it is ActivityNotFoundException) {
            Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}
