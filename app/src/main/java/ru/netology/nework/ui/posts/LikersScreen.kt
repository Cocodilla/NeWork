package ru.netology.nework.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.netology.nework.R
import ru.netology.nework.model.User
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.ui.theme.NeWorkFontWeights

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikersScreen(
    navController: NavController,
    postId: Long,
    viewModel: PostDetailsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    Scaffold(
        containerColor = NeWorkColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_likers)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeWorkColors.ScreenBackground,
                ),
            )
        },
    ) { paddingValues ->
        when {
            uiState.loading -> {
                LoadingState()
            }

            uiState.error != null -> {
                ErrorState(
                    onRetry = { viewModel.load(postId) },
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.likers,
                        key = { user: User -> user.id },
                    ) { user: User ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NeWorkColors.CardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeWorkColors.AvatarBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        color = NeWorkColors.AccentPrimary,
                                        fontWeight = NeWorkFontWeights.Bold,
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = user.name,
                                        fontWeight = NeWorkFontWeights.SemiBold,
                                        color = NeWorkColors.TextPrimarySoft,
                                    )
                                    Text(
                                        text = user.login,
                                        color = NeWorkColors.TextMutedSoft,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
