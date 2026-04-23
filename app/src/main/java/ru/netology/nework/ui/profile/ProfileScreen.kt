package ru.netology.nework.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.ExternalLinkText
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.users.UserAvatar
import ru.netology.nework.ui.users.UserHero

private val ProfileBg = Color(0xFFF7F2FA)
private val ProfileSurface = Color(0xFFF8F1FB)
private val ProfileBorder = Color(0xFFE2D5EC)
private val ProfileAccent = Color(0xFF6F52B5)
private val ProfileMuted = Color(0xFF7C7288)
private val ProfileText = Color(0xFF231B2F)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MyProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTab by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = ProfileBg,
        topBar = {
            TopAppBar(
                title = { Text("You") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfileBg,
                ),
            )
        },
        floatingActionButton = {
            if (selectedTab == 1 && state.user != null && !state.loading && state.error == null) {
                FloatingActionButton(
                    onClick = { navController.navigate(Destination.JobEditor.createRoute()) },
                    containerColor = Color(0xFFE7D9F5),
                    contentColor = ProfileAccent,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить работу",
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            state.loading -> LoadingState()

            state.error != null -> ErrorState(
                onRetry = { viewModel.load() }
            )

            else -> {
                val user = state.user
                if (user == null) {
                    ErrorState(onRetry = { viewModel.load() })
                    return@Scaffold
                }

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
                            containerColor = ProfileBg,
                            contentColor = ProfileAccent,
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Wall") },
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Jobs") },
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        if (state.wall.isEmpty()) {
                            item {
                                EmptyProfileSection(text = "На стене пока нет записей")
                            }
                        } else {
                            items(state.wall, key = { it.id }) { post ->
                                MyProfileWallCard(
                                    user = user,
                                    post = post,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                    } else {
                        if (state.jobs.isEmpty()) {
                            item {
                                EmptyProfileSection(text = "Работы пока не добавлены")
                            }
                        } else {
                            items(state.jobs, key = { it.id }) { job ->
                                MyProfileJobCard(
                                    job = job,
                                    onDelete = { viewModel.removeJob(job.id) },
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

@Composable
private fun EmptyProfileSection(
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
            color = ProfileMuted,
        )
    }
}

@Composable
private fun MyProfileWallCard(
    user: User,
    post: Post,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ProfileBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    user = user,
                    size = 38.dp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileText,
                    )
                    Text(
                        text = post.published,
                        color = ProfileMuted,
                    )
                }
            }

            Text(
                text = post.content,
                color = ProfileText,
            )

            if (!post.mediaUrl.isNullOrBlank() && post.mediaType != PostMediaType.NONE) {
                SubcomposeAsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Пост профиля",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .background(Color.Transparent),
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
                                            colors = listOf(Color(0xFFE6DAF4), Color(0xFFD2BCED))
                                        )
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Photo",
                                    color = ProfileAccent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE8DEEF))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = ProfileAccent,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = post.likes.toString(),
                    color = ProfileAccent,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = ProfileAccent,
                )
            }
        }
    }
}

@Composable
private fun MyProfileJobCard(
    job: Job,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ProfileBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = job.name,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileText,
                    )
                    Text(
                        text = "${job.start} — ${job.finish ?: "НВ"}",
                        color = ProfileMuted,
                    )
                    Text(
                        text = job.position,
                        color = ProfileText,
                    )
                    job.link?.takeIf { it.isNotBlank() }?.let { link ->
                        ExternalLinkText(
                            url = link,
                            color = ProfileAccent,
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить работу",
                        tint = ProfileText,
                    )
                }
            }
        }
    }
}
