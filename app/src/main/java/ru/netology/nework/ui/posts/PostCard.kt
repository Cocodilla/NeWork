package ru.netology.nework.ui.posts

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.ui.common.ExternalLinkText

private val CardBg = Color(0xFFF5EEF8)
private val CardBorder = Color(0xFFDCCEEA)
private val Accent = Color(0xFF6F52B5)
private val Muted = Color(0xFF6B6B6B)

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (post.authorAvatar.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8CBE7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = post.author.firstOrNull()?.uppercase() ?: "?",
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    AsyncImage(
                        model = post.authorAvatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(modifier = Modifier.size(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.author,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2B2B2B),
                    )
                    Text(
                        text = post.published,
                        color = Muted,
                    )
                }

                if (post.ownedByMe) {
                    Box {
                        IconButton(
                            onClick = {
                                expanded = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = Muted,
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (post.mediaType != PostMediaType.NONE && !post.mediaUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = "Post media",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.55f),
                        contentScale = ContentScale.Crop,
                    )

                    if (post.mediaType == PostMediaType.VIDEO) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircleFilled,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = post.content,
                color = Color(0xFF2B2B2B),
            )

            post.link?.takeIf { it.isNotBlank() }?.let { link ->
                Spacer(modifier = Modifier.height(10.dp))
                ExternalLinkText(
                    url = link,
                    color = Accent,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFFE6DDF0))

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Accent,
                    )
                }

                Text(
                    text = post.likes.toString(),
                    color = Accent,
                )

                Spacer(modifier = Modifier.size(8.dp))

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Accent,
                    )
                }
            }
        }
    }
}
