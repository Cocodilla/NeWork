package ru.netology.nework.ui.posts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.netology.nework.R
import ru.netology.nework.navigation.Destination
import ru.netology.nework.model.AttachmentType
import ru.netology.nework.ui.common.EditorAttachmentPreview
import ru.netology.nework.ui.common.LocationPreviewCard
import ru.netology.nework.ui.common.SelectedUsersCard
import ru.netology.nework.ui.common.UserPickerDialog
import ru.netology.nework.ui.common.handlePickedAttachment
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.util.toCoordinatesOrNull
private const val POST_LOCATION_RESULT_KEY = "post_location_result"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    postId: Long,
    viewModel: EditPostViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val savedStateHandle = remember { navController.currentBackStackEntry?.savedStateHandle }
    val locationResultFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow(POST_LOCATION_RESULT_KEY, "")
    }
    val locationResult by locationResultFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf("") }
    var showUserPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedAttachment(
            context = context,
            uri = uri,
            allowedTypes = setOf(AttachmentType.IMAGE),
            onAttachmentPicked = viewModel::changeAttachment,
        )
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedAttachment(
            context = context,
            uri = uri,
            allowedTypes = setOf(AttachmentType.VIDEO),
            onAttachmentPicked = viewModel::changeAttachment,
        )
    }

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    LaunchedEffect(locationResult) {
        if (locationResult.isNotBlank()) {
            viewModel.changeCoordinates(locationResult.toCoordinatesOrNull())
            savedStateHandle?.set(POST_LOCATION_RESULT_KEY, "")
        }
    }

    if (showUserPicker) {
        UserPickerDialog(
            title = stringResource(R.string.post_editor_mark_people),
            users = state.availableUsers,
            initiallySelectedIds = state.mentionIds.toSet(),
            onDismiss = { showUserPicker = false },
            onConfirm = { selectedIds ->
                viewModel.changeMentionIds(selectedIds)
                showUserPicker = false
            },
        )
    }

    Scaffold(
        containerColor = NeWorkColors.EditorBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.id == 0L) {
                            stringResource(R.string.post_editor_title_new)
                        } else {
                            stringResource(R.string.post_editor_title_edit)
                        },
                        color = NeWorkColors.AccentDark,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = NeWorkColors.AccentDark,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.save(
                                onSaved = { navController.popBackStack() }
                            )
                        },
                        enabled = state.canSave,
                    ) {
                        if (state.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = NeWorkColors.AccentDark,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.cd_save),
                                tint = NeWorkColors.AccentDark,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeWorkColors.EditorBackground,
                ),
                modifier = Modifier.statusBarsPadding(),
            )
        },
        bottomBar = {
            PostEditorBottomBar(
                onPickImage = { imagePicker.launch("image/*") },
                onPickVideo = { videoPicker.launch("video/*") },
                onPickPeople = { showUserPicker = true },
                onPickLocation = {
                    navController.navigate(
                        Destination.LocationPicker.createRoute(
                            resultKey = POST_LOCATION_RESULT_KEY,
                            initialCoordinates = state.coordinates,
                        )
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            BasicTextField(
                value = state.content,
                onValueChange = viewModel::changeContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                textStyle = MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(
                        color = NeWorkColors.AccentDark,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    )
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (state.content.isBlank()) {
                            Text(
                                text = stringResource(R.string.post_editor_placeholder),
                                color = NeWorkColors.Placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            EditorAttachmentPreview(
                attachment = state.attachment,
                existingMediaUrl = state.existingMediaUrl,
                existingMediaType = state.existingMediaType,
                onRemove = viewModel::clearAttachment,
            )

            SelectedUsersCard(
                title = stringResource(R.string.post_editor_selected_users),
                users = state.availableUsers.filter { user -> user.id in state.mentionIds },
            )

            state.coordinates?.let { coordinates ->
                LocationPreviewCard(
                    title = stringResource(R.string.post_editor_location_title),
                    coordinates = coordinates,
                    onRemove = viewModel::clearCoordinates,
                )
            }

            state.saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PostEditorBottomBar(
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickPeople: () -> Unit,
    onPickLocation: () -> Unit,
) {
    NavigationBar(
        containerColor = NeWorkColors.EditorBar,
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onPickImage,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.attachment_photo),
                )
            },
            label = { Text(stringResource(R.string.attachment_photo)) },
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickVideo,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = stringResource(R.string.attachment_video),
                )
            },
            label = { Text(stringResource(R.string.attachment_video)) },
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickPeople,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = stringResource(R.string.attachment_people),
                )
            },
            label = { Text(stringResource(R.string.attachment_people)) },
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickLocation,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = stringResource(R.string.attachment_place),
                )
            },
            label = { Text(stringResource(R.string.attachment_place)) },
        )
    }
}
