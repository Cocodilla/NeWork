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
import ru.netology.nework.ui.theme.NeWorkColors

object AuthUi {
    val ScreenBackground = NeWorkColors.ScreenBackground
    val AppBarBackground = NeWorkColors.ScreenBackground

    val FieldContainer = NeWorkColors.AuthFieldContainer
    val FieldBorder = NeWorkColors.AccentSecondary
    val FieldLabel = NeWorkColors.AccentSecondary
    val ErrorColor = NeWorkColors.Error

    val PrimaryButton = NeWorkColors.AccentPrimary
    val PrimaryButtonText = Color.White

    val SecondaryText = NeWorkColors.AccentPrimary

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
            .background(NeWorkColors.AvatarPlaceholder),
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
    unfocusedBorderColor = NeWorkColors.AuthFieldBorderUnfocused,
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
