package ru.netology.nework.ui.posts

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.netology.nework.R
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.AppBottomBar
import ru.netology.nework.ui.common.AuthAwareTopBar
import ru.netology.nework.ui.common.AuthRequiredDialog
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.auth.AuthViewModel
import ru.netology.nework.ui.theme.NeWorkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    navController: NavController,
    viewModel: PostsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var authDialogMessageRes by remember { mutableStateOf<Int?>(null) }

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
            AuthAwareTopBar(
                navController = navController,
                isAuthorized = authState.authorized,
                onLogout = authViewModel::logout,
                title = stringResource(R.string.screen_posts),
                containerColor = NeWorkColors.ScreenBackground,
                contentColor = NeWorkColors.AccentPrimary,
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (authState.authorized) {
                        navController.navigate(Destination.PostEditor.createRoute())
                    } else {
                        authDialogMessageRes = R.string.auth_required_post_message
                    }
                },
                containerColor = NeWorkColors.FabBackground,
                contentColor = NeWorkColors.AccentPrimary,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        }
    ) { paddingValues ->
        when {
            state.loading && state.data.isEmpty() -> LoadingState()
            state.error != null && state.data.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = { viewModel.load() },
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.error?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }

                    items(state.data, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onClick = {
                                navController.navigate(Destination.PostDetails.createRoute(post.id))
                            },
                            onLike = {
                                if (authState.authorized) {
                                    viewModel.onLike(post)
                                } else {
                                    authDialogMessageRes = R.string.auth_required_post_like_message
                                }
                            },
                            likeEnabled = !post.ownedByMe,
                            onShare = {
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
                            },
                            onEdit = {
                                navController.navigate(Destination.PostEditor.createRoute(post.id))
                            },
                            onDelete = { viewModel.onDelete(post.id) },
                        )
                    }
                }
            }
        }
    }
}
