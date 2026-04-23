package ru.netology.nework.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AuthUi {
    val ScreenBackground = Color(0xFFF7F2FA)
    val AppBarBackground = Color(0xFFF7F2FA)

    val FieldContainer = Color(0xFFEDE7F6)
    val FieldBorder = Color(0xFF7E57C2)
    val FieldLabel = Color(0xFF7E57C2)
    val ErrorColor = Color(0xFFD32F2F)

    val PrimaryButton = Color(0xFF6F52B5)
    val PrimaryButtonText = Color.White

    val SecondaryText = Color(0xFF6F52B5)

    val FieldShape = RoundedCornerShape(4.dp)
    val ButtonShape = RoundedCornerShape(24.dp)
}

@Composable
fun AuthScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuthUi.ScreenBackground),
    ) {
        content()
    }
}

@Composable
fun AuthAvatarPlaceholder(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFD6D6D6)),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AuthUi.FieldContainer,
    unfocusedContainerColor = AuthUi.FieldContainer,
    disabledContainerColor = AuthUi.FieldContainer,
    focusedBorderColor = AuthUi.FieldBorder,
    unfocusedBorderColor = Color(0xFFB39DDB),
    focusedLabelColor = AuthUi.FieldLabel,
    unfocusedLabelColor = AuthUi.FieldLabel,
    cursorColor = AuthUi.FieldBorder,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
)

@Composable
fun AuthErrorText(message: String) {
    Text(
        text = message,
        color = AuthUi.ErrorColor,
        style = MaterialTheme.typography.bodySmall,
    )
}