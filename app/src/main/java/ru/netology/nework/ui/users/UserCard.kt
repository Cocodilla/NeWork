package ru.netology.nework.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.netology.nework.model.User
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.ui.theme.NeWorkFontWeights

@Composable
fun UserListCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeWorkColors.SurfacePrimary),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(NeWorkColors.BorderPrimary)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                user = user,
                size = 44.dp,
            )

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = user.name,
                    color = NeWorkColors.TextPrimary,
                    fontWeight = NeWorkFontWeights.SemiBold,
                )
                Text(
                    text = user.login,
                    color = NeWorkColors.TextMuted,
                )
            }
        }
    }
}

@Composable
fun UserHero(
    user: User,
    modifier: Modifier = Modifier,
) {
    val heroModifier = modifier
        .fillMaxWidth()
        .aspectRatio(1.18f)
        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))

    if (user.avatar.isNullOrBlank()) {
        HeroFallback(
            user = user,
            modifier = heroModifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = user.avatar,
            contentDescription = user.name,
            modifier = heroModifier,
            contentScale = ContentScale.Crop,
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> HeroFallback(user = user, modifier = heroModifier)
            }
        }
    }
}

@Composable
fun UserAvatar(
    user: User,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    if (user.avatar.isNullOrBlank()) {
        AvatarFallback(
            user = user,
            size = size,
            modifier = modifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = user.avatar,
            contentDescription = user.name,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> AvatarFallback(
                    user = user,
                    size = size,
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
private fun HeroFallback(
    user: User,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(NeWorkColors.GradientStart, NeWorkColors.GradientEnd)
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AvatarFallback(
                user = user,
                size = 84.dp,
            )
            Text(
                text = user.name,
                color = NeWorkColors.TextPrimary,
                fontWeight = NeWorkFontWeights.Bold,
            )
        }
    }
}

@Composable
private fun AvatarFallback(
    user: User,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(NeWorkColors.AvatarBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = user.name.firstOrNull()?.uppercase() ?: "?",
            color = NeWorkColors.AccentPrimary,
            fontWeight = NeWorkFontWeights.Bold,
        )
    }
}
