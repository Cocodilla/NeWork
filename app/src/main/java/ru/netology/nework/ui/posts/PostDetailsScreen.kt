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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.netology.nework.R
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.auth.AuthViewModel
import ru.netology.nework.ui.common.AuthRequiredDialog
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.ExternalLinkText
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.common.SelectedUsersCard
import ru.netology.nework.ui.map.StaticLocationMap
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.ui.theme.NeWorkFontWeights
import ru.netology.nework.util.toDisplayDateTimeOrSelf
import ru.netology.nework.util.toDisplayString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    postId: Long,
    viewModel: PostDetailsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var authDialogMessageRes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    authDialogMessageRes?.let { messageRes ->
        AuthRequiredDialog(
            title = stringResource(R.string.auth_required_title),
            message = stringResource(messageRes),
            onLogin = {
                authDialogMessageRes = null
                navController.navigate(Destination.Login.route)
            },
            onRegister = {
                authDialogMessageRes = null
                navController.navigate(Destination.Register.route)
            },
            onDismiss = { authDialogMessageRes = null },
        )
    }

    Scaffold(
        containerColor = NeWorkColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_post)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
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
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.action_share_post),
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.cd_share),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeWorkColors.ScreenBackground
                )
            )
        }
    ) { paddingValues ->
        when {
            state.loading && state.post == null -> {
                LoadingState()
            }

            state.post == null -> {
                ErrorState(
                    message = state.error,
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
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    if (!post.mediaUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = post.mediaUrl,
                            contentDescription = stringResource(R.string.cd_post_media),
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
                            fontWeight = NeWorkFontWeights.SemiBold,
                        )

                        Text(
                            text = post.authorJob?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.job_searching),
                            color = Color.Gray,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = post.published.toDisplayDateTimeOrSelf(),
                            color = Color.Gray,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = post.content)

                        post.link?.takeIf { it.isNotBlank() }?.let { link ->
                            Spacer(modifier = Modifier.height(10.dp))
                            ExternalLinkText(url = link, color = NeWorkColors.AccentPrimary)
                        }

                        post.coordinates?.let { coordinates ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(
                                    R.string.post_location_format,
                                    coordinates.toDisplayString(),
                                ),
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
                            IconButton(
                                onClick = {
                                    if (authState.authorized) {
                                        viewModel.onLike()
                                    } else {
                                        authDialogMessageRes = R.string.auth_required_post_like_message
                                    }
                                },
                                enabled = !post.ownedByMe,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = stringResource(R.string.cd_liked),
                                    tint = NeWorkColors.AccentPrimary,
                                )
                            }

                            Text(
                                text = post.likes.toString(),
                                color = NeWorkColors.AccentPrimary,
                            )
                        }

                        SelectedUsersCard(
                            title = stringResource(R.string.post_mentioned_users),
                            users = state.mentionedUsers,
                        )
                    }
                }
            }
        }
    }
}
