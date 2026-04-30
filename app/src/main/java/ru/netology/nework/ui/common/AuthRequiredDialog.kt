package ru.netology.nework.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.netology.nework.R

@Composable
fun AuthRequiredDialog(
    title: String,
    message: String,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onLogin) {
                Text(stringResource(R.string.action_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onRegister) {
                Text(stringResource(R.string.action_register))
            }
        },
    )
}
