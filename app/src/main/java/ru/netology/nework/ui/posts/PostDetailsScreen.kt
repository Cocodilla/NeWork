package ru.netology.nework.ui.posts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.ExternalLinkText
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.common.SelectedUsersCard
import ru.netology.nework.ui.map.StaticLocationMap
import ru.netology.nework.util.toDisplayString

private val ScreenBg = Color(0xFFF7F2FA)
private val Accent = Color(0xFF6F52B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    postId: Long,
    viewModel: PostDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Пост") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val post = state.post ?: return@IconButton
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${post.author}\n\n${post.content}")
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Поделиться постом")
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Поделиться",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBg
                )
            )
        }
    ) { paddingValues ->
        when {
            state.loading -> {
                LoadingState()
            }

            state.error != null -> {
                ErrorState(
                    onRetry = { viewModel.load(postId) }
                )
            }

            state.post == null -> {
                ErrorState(
                    onRetry = { viewModel.load(postId) }
                )
            }

            else -> {
                val post = state.post!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (!post.mediaUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = post.mediaUrl,
                            contentDescription = "Post media",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = post.author,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = post.authorJob?.takeIf { it.isNotBlank() } ?: "В поиске работы",
                            color = Color.Gray,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = post.published,
                            color = Color.Gray,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = post.content)

                        post.link?.takeIf { it.isNotBlank() }?.let { link ->
                            Spacer(modifier = Modifier.height(10.dp))
                            ExternalLinkText(url = link, color = Accent)
                        }

                        post.coordinates?.let { coordinates ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Локация: ${coordinates.toDisplayString()}",
                                color = Color.Gray,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StaticLocationMap(
                                coordinates = coordinates,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::onLike) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Нравится",
                                    tint = Accent,
                                )
                            }

                            Text(
                                text = post.likes.toString(),
                                color = Accent,
                            )
                        }

                        SelectedUsersCard(
                            title = "Упомянутые пользователи",
                            users = state.mentionedUsers,
                        )
                    }
                }
            }
        }
    }
}
