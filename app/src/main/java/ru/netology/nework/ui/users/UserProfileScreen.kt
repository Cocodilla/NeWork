package ru.netology.nework.ui.users

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.netology.nework.R
import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.ExternalLinkText
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.ui.theme.NeWorkFontWeights
import ru.netology.nework.util.toDisplayDateOrSelf
import ru.netology.nework.util.toDisplayDateTimeOrSelf

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: Long,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    Scaffold(
        containerColor = NeWorkColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.user?.let { "${it.name} / ${it.login}" }
                            ?: stringResource(R.string.screen_user_fallback),
                    )
                },
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
        }
    ) { paddingValues ->
        when {
            state.loading -> LoadingState()

            state.error != null -> ErrorState(
                onRetry = { viewModel.load(userId) }
            )

            else -> {
                val user = state.user
                if (user == null) {
                    ErrorState(onRetry = { viewModel.load(userId) })
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            UserHero(user = user)
                        }

                        stickyHeader {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = NeWorkColors.ScreenBackground,
                                contentColor = NeWorkColors.AccentPrimary,
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text(stringResource(R.string.tab_wall)) },
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text(stringResource(R.string.tab_jobs)) },
                                )
                            }
                        }

                        if (selectedTab == 0) {
                            if (state.wall.isEmpty()) {
                                item {
                                    EmptyUserSection(text = stringResource(R.string.empty_wall))
                                }
                            } else {
                                items(state.wall, key = { it.id }) { post ->
                                    UserWallCard(
                                        post = post,
                                        onClick = {
                                            navController.navigate(
                                                Destination.PostDetails.createRoute(post.id)
                                            )
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        } else {
                            if (state.jobs.isEmpty()) {
                                item {
                                    EmptyUserSection(text = stringResource(R.string.empty_jobs))
                                }
                            } else {
                                items(state.jobs, key = { it.id }) { job ->
                                    UserJobCard(
                                        job = job,
                                        modifier = Modifier.padding(horizontal = 16.dp),
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

@Composable
private fun EmptyUserSection(
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = NeWorkColors.TextMuted,
        )
    }
}

@Composable
private fun UserWallCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeWorkColors.SurfacePrimary),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(NeWorkColors.BorderPrimary)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    user = User(
                        id = post.authorId,
                        login = post.author.lowercase(),
                        name = post.author,
                        avatar = post.authorAvatar,
                        job = post.authorJob,
                    ),
                    size = 38.dp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                androidx.compose.foundation.layout.Column {
                    Text(
                        text = post.author,
                        fontWeight = NeWorkFontWeights.SemiBold,
                        color = NeWorkColors.TextPrimary,
                    )
                    Text(
                        text = post.published.toDisplayDateTimeOrSelf(),
                        color = NeWorkColors.TextMuted,
                    )
                }
            }

            Text(
                text = post.content,
                color = NeWorkColors.TextPrimary,
            )

            if (!post.mediaUrl.isNullOrBlank() && post.mediaType != PostMediaType.NONE) {
                SubcomposeAsyncImage(
                    model = post.mediaUrl,
                    contentDescription = stringResource(R.string.user_post_media),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(NeWorkColors.GradientStart, NeWorkColors.GradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.attachment_photo),
                                    color = NeWorkColors.AccentPrimary,
                                    fontWeight = NeWorkFontWeights.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = NeWorkColors.Divider)

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = NeWorkColors.AccentPrimary,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = post.likes.toString(),
                    color = NeWorkColors.AccentPrimary,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = NeWorkColors.AccentPrimary,
                )
            }
        }
    }
}

@Composable
private fun UserJobCard(
    job: Job,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeWorkColors.SurfacePrimary),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(NeWorkColors.BorderPrimary)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = job.name,
                fontWeight = NeWorkFontWeights.SemiBold,
                color = NeWorkColors.TextPrimary,
            )
            Text(
                text = stringResource(
                    R.string.job_date_range,
                    job.start.toDisplayDateOrSelf(),
                    job.finish?.toDisplayDateOrSelf() ?: stringResource(R.string.job_now_short),
                ),
                color = NeWorkColors.TextMuted,
            )
            Text(
                text = job.position,
                color = NeWorkColors.TextPrimary,
            )
            job.link?.takeIf { it.isNotBlank() }?.let { link ->
                ExternalLinkText(
                    url = link,
                    color = NeWorkColors.AccentPrimary,
                )
            }
        }
    }
}
