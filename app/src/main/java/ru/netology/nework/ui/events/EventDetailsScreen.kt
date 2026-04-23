package ru.netology.nework.ui.events

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.netology.nework.model.Event
import ru.netology.nework.model.EventType
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User
import ru.netology.nework.ui.auth.AuthViewModel
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.AuthRequiredDialog
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.ExternalLinkText
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.map.StaticLocationMap

private val EventDetailsBackground = Color(0xFFF7F2FA)
private val EventDetailsSurface = Color(0xFFF8F1FB)
private val EventDetailsBorder = Color(0xFFE2D5EC)
private val EventDetailsAccent = Color(0xFF6F52B5)
private val EventDetailsAvatarBg = Color(0xFFD8CBE7)
private val EventDetailsText = Color(0xFF251C31)
private val EventDetailsMuted = Color(0xFF7B7089)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: Long,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.load(eventId)
    }

    if (showAuthDialog) {
        AuthRequiredDialog(
            title = "Нужен аккаунт",
            message = "Чтобы участвовать в событии, войди в аккаунт или зарегистрируйся.",
            onLogin = {
                showAuthDialog = false
                navController.navigate(Destination.Login.route)
            },
            onRegister = {
                showAuthDialog = false
                navController.navigate(Destination.Register.route)
            },
            onDismiss = { showAuthDialog = false },
        )
    }

    if (dialogTitle != null) {
        EventUsersDialog(
            title = dialogTitle.orEmpty(),
            users = dialogUsers,
            onDismiss = {
                dialogTitle = null
                dialogUsers = emptyList()
            },
        )
    }

    Scaffold(
        containerColor = EventDetailsBackground,
        topBar = {
            TopAppBar(
                title = { Text("Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = EventDetailsText,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val event = state.event ?: return@IconButton
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    buildString {
                                        append(event.author)
                                        append("\n")
                                        append(event.datetime)
                                        append("\n\n")
                                        append(event.content)
                                    }
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Поделиться событием")
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Поделиться",
                            tint = EventDetailsText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EventDetailsBackground,
                ),
            )
        },
    ) { paddingValues ->
        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(onRetry = { viewModel.load(eventId) })
            state.event == null -> ErrorState(onRetry = { viewModel.load(eventId) })
            else -> {
                val event = state.event!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = EventDetailsSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(EventDetailsBorder)
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                EventHeader(event = event)

                                if (!event.mediaUrl.isNullOrBlank()) {
                                    EventMediaBlock(event = event)
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = event.type.toDisplayName(),
                                        fontWeight = FontWeight.SemiBold,
                                        color = EventDetailsText,
                                    )
                                    Text(
                                        text = event.datetime,
                                        color = EventDetailsMuted,
                                    )
                                    Text(
                                        text = event.content,
                                        color = EventDetailsText,
                                    )
                                    event.link?.takeIf { it.isNotBlank() }?.let { link ->
                                        ExternalLinkText(
                                            url = link,
                                            color = EventDetailsAccent,
                                        )
                                    }
                                }

                                EventParticipationButton(
                                    participatedByMe = event.participatedByMe,
                                    onClick = {
                                        if (authState.authorized) {
                                            viewModel.onParticipate()
                                        } else {
                                            showAuthDialog = true
                                        }
                                    },
                                )

                                EventMetricSection(
                                    title = "Likers",
                                    count = event.likeOwnerIds.size,
                                    users = state.likers,
                                    onMoreClick = if (state.likers.isNotEmpty()) {
                                        {
                                            dialogTitle = "Likers"
                                            dialogUsers = state.likers.distinctBy(User::id)
                                        }
                                    } else {
                                        null
                                    },
                                    leading = {
                                        IconButton(
                                            onClick = viewModel::onLike,
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (event.likedByMe) {
                                                    Icons.Filled.Favorite
                                                } else {
                                                    Icons.Outlined.FavoriteBorder
                                                },
                                                contentDescription = "Нравится",
                                                tint = EventDetailsAccent,
                                            )
                                        }
                                    },
                                )

                                if (state.speakers.isNotEmpty()) {
                                    EventPeopleSection(
                                        title = "Speakers",
                                        users = state.speakers,
                                        onMoreClick = {
                                            dialogTitle = "Speakers"
                                            dialogUsers = state.speakers.distinctBy(User::id)
                                        },
                                    )
                                }

                                EventMetricSection(
                                    title = "Participants",
                                    count = event.participantsIds.size,
                                    users = state.participants,
                                    onMoreClick = if (state.participants.isNotEmpty()) {
                                        {
                                            dialogTitle = "Participants"
                                            dialogUsers = state.participants.distinctBy(User::id)
                                        }
                                    } else {
                                        null
                                    },
                                    leading = {
                                        Icon(
                                            imageVector = Icons.Outlined.People,
                                            contentDescription = null,
                                            tint = EventDetailsAccent,
                                        )
                                    },
                                )

                                event.coordinates?.let { coordinates ->
                                    HorizontalDivider(color = Color(0xFFE8DEEF))
                                    Text(
                                        text = "Location",
                                        fontWeight = FontWeight.SemiBold,
                                        color = EventDetailsText,
                                    )
                                    StaticLocationMap(
                                        coordinates = coordinates,
                                        modifier = Modifier.fillMaxWidth(),
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
private fun EventParticipationButton(
    participatedByMe: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (participatedByMe) {
                Color(0xFFE7D9F5)
            } else {
                EventDetailsAccent
            },
            contentColor = if (participatedByMe) {
                EventDetailsAccent
            } else {
                Color.White
            },
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(if (participatedByMe) "Вы участвуете" else "Участвовать")
    }
}

@Composable
private fun EventHeader(
    event: Event,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventAvatar(
            user = User(
                id = event.authorId,
                login = event.author.lowercase().replace(" ", ""),
                name = event.author.ifBlank { "Speaker" },
                avatar = event.authorAvatar,
                job = event.authorJob,
                about = null,
            ),
            size = 42.dp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = event.author.ifBlank { "Unknown speaker" },
                fontWeight = FontWeight.SemiBold,
                color = EventDetailsText,
            )
            Text(
                text = event.authorJob?.takeIf { it.isNotBlank() } ?: "В поиске работы",
                color = EventDetailsMuted,
            )
            Text(
                text = event.published,
                color = EventDetailsMuted,
            )
        }
    }
}

@Composable
private fun EventMediaBlock(
    event: Event,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.32f)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = event.mediaUrl,
            contentDescription = "Обложка события",
            modifier = Modifier.fillMaxSize(),
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
                            text = event.type.toDisplayName(),
                            color = EventDetailsAccent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        if (event.mediaType == PostMediaType.VIDEO) {
            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = "Видео",
                tint = Color.White,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@Composable
private fun EventPeopleSection(
    title: String,
    users: List<User>,
    onMoreClick: (() -> Unit)? = null,
) {
    HorizontalDivider(color = Color(0xFFE8DEEF))
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        color = EventDetailsText,
    )
    AvatarPreviewRow(
        users = users,
        totalCount = users.size,
        onMoreClick = onMoreClick,
    )
}

@Composable
private fun EventMetricSection(
    title: String,
    count: Int,
    users: List<User>,
    onMoreClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
) {
    HorizontalDivider(color = Color(0xFFE8DEEF))
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        color = EventDetailsText,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading()
        Text(
            text = count.toString(),
            color = EventDetailsAccent,
            fontWeight = FontWeight.SemiBold,
        )
        AvatarPreviewRow(
            users = users,
            totalCount = count,
            onMoreClick = onMoreClick,
        )
    }
}

@Composable
private fun AvatarPreviewRow(
    users: List<User>,
    totalCount: Int,
    onMoreClick: (() -> Unit)? = null,
) {
    val previewUsers = users.distinctBy(User::id).take(5)
    val extraCount = (totalCount - previewUsers.size).coerceAtLeast(0)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        previewUsers.forEach { user ->
            EventAvatar(user = user, size = 32.dp)
        }

        if (extraCount > 0 && onMoreClick != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EventDetailsAccent)
                    .clickable {
                        onMoreClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun EventUsersDialog(
    title: String,
    users: List<User>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = users.distinctBy(User::id),
                    key = { it.id },
                ) { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EventAvatar(user = user, size = 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.SemiBold,
                                color = EventDetailsText,
                            )
                            user.job?.takeIf { it.isNotBlank() }?.let { job ->
                                Text(
                                    text = job,
                                    color = EventDetailsMuted,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
    )
}

@Composable
private fun EventAvatar(
    user: User,
    size: Dp,
) {
    if (user.avatar.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(EventDetailsAvatarBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercase() ?: "?",
                color = EventDetailsAccent,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = user.avatar,
            contentDescription = user.name,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(EventDetailsAvatarBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercase() ?: "?",
                            color = EventDetailsAccent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private fun EventType.toDisplayName(): String = when (this) {
    EventType.ONLINE -> "Online"
    EventType.OFFLINE -> "Offline"
}
