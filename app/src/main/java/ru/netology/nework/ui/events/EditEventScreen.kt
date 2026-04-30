package ru.netology.nework.ui.events

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import ru.netology.nework.model.EventType
import ru.netology.nework.ui.common.EditorAttachmentPreview
import ru.netology.nework.ui.common.LocationPreviewCard
import ru.netology.nework.ui.common.SelectedUsersCard
import ru.netology.nework.ui.common.UserPickerDialog
import ru.netology.nework.ui.common.handlePickedAttachment
import ru.netology.nework.util.toCoordinatesOrNull

private val EventEditorBackground = Color(0xFFFFFBFF)
private val EventEditorBar = Color(0xFFF4EEF8)
private val EventEditorAccent = Color(0xFF2B1D3F)
private val EventEditorPlaceholder = Color(0xFF857A92)
private const val EVENT_LOCATION_RESULT_KEY = "event_location_result"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: Long,
    viewModel: EditEventViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val savedStateHandle = remember { navController.currentBackStackEntry?.savedStateHandle }
    val locationResultFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow(EVENT_LOCATION_RESULT_KEY, "")
    }
    val locationResult by locationResultFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf("") }
    var showSpeakerPicker by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedAttachment(
            context = context,
            uri = uri,
            allowedTypes = setOf(AttachmentType.IMAGE),
            onAttachmentPicked = viewModel::changeAttachment,
        )
    }

    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedAttachment(
            context = context,
            uri = uri,
            allowedTypes = setOf(AttachmentType.IMAGE, AttachmentType.AUDIO, AttachmentType.VIDEO),
            onAttachmentPicked = viewModel::changeAttachment,
        )
    }

    LaunchedEffect(eventId) {
        viewModel.load(eventId)
    }

    LaunchedEffect(locationResult) {
        if (locationResult.isNotBlank()) {
            viewModel.changeCoordinates(locationResult.toCoordinatesOrNull())
            savedStateHandle?.set(EVENT_LOCATION_RESULT_KEY, "")
        }
    }

    if (showSpeakerPicker) {
        UserPickerDialog(
            title = stringResource(R.string.event_editor_choose_speakers),
            users = state.availableUsers,
            initiallySelectedIds = state.speakerIds.toSet(),
            onDismiss = { showSpeakerPicker = false },
            onConfirm = { selectedIds ->
                viewModel.changeSpeakerIds(selectedIds)
                showSpeakerPicker = false
            },
        )
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.event_editor_settings_title),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = state.datetime,
                    onValueChange = viewModel::changeDateTime,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.event_editor_datetime_label)) },
                    supportingText = { Text(stringResource(R.string.event_editor_datetime_hint)) },
                )

                Text(
                    text = stringResource(R.string.event_editor_type_label),
                    style = MaterialTheme.typography.titleMedium,
                )

                EventTypeOption(
                    selected = state.type == EventType.ONLINE,
                    title = stringResource(R.string.event_type_online),
                    onClick = { viewModel.changeType(EventType.ONLINE) },
                )
                EventTypeOption(
                    selected = state.type == EventType.OFFLINE,
                    title = stringResource(R.string.event_type_offline),
                    onClick = { viewModel.changeType(EventType.OFFLINE) },
                )

                if (state.type == EventType.OFFLINE && state.coordinates == null) {
                    Text(
                        text = stringResource(R.string.event_editor_offline_location_hint),
                        color = EventEditorPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                TextButton(
                    onClick = { showSettingsSheet = false },
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Text(stringResource(R.string.action_done))
                }
            }
        }
    }

    Scaffold(
        containerColor = EventEditorBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.id == 0L) {
                            stringResource(R.string.event_editor_title_new)
                        } else {
                            stringResource(R.string.event_editor_title_edit)
                        },
                        color = EventEditorAccent,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = EventEditorAccent,
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
                                color = EventEditorAccent,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.cd_save),
                                tint = EventEditorAccent,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EventEditorBackground,
                ),
                modifier = Modifier.statusBarsPadding(),
            )
        },
        bottomBar = {
            EventEditorBottomBar(
                onPickPhoto = { photoPicker.launch("image/*") },
                onPickAttachment = { attachmentPicker.launch("*/*") },
                onPickSpeakers = { showSpeakerPicker = true },
                onPickLocation = {
                    navController.navigate(
                        Destination.LocationPicker.createRoute(
                            resultKey = EVENT_LOCATION_RESULT_KEY,
                            initialCoordinates = state.coordinates,
                        )
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSettingsSheet = true },
                containerColor = Color(0xFFE7D9F5),
                contentColor = EventEditorAccent,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_event_settings),
                )
            }
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
                    .height(210.dp),
                textStyle = MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(
                        color = EventEditorAccent,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    )
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (state.content.isBlank()) {
                            Text(
                                text = stringResource(R.string.event_editor_placeholder),
                                color = EventEditorPlaceholder,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Text(
                text = stringResource(
                    R.string.event_editor_summary,
                    stringResource(state.type.toDisplayNameRes()),
                    state.datetime,
                ),
                color = EventEditorPlaceholder,
                style = MaterialTheme.typography.bodyMedium,
            )

            EditorAttachmentPreview(
                attachment = state.attachment,
                existingMediaUrl = state.existingMediaUrl,
                existingMediaType = state.existingMediaType,
                onRemove = viewModel::clearAttachment,
            )

            SelectedUsersCard(
                title = stringResource(R.string.event_editor_speakers),
                users = state.availableUsers.filter { user -> user.id in state.speakerIds },
            )

            state.coordinates?.let { coordinates ->
                LocationPreviewCard(
                    title = stringResource(R.string.event_editor_location_title),
                    coordinates = coordinates,
                    onRemove = viewModel::clearCoordinates,
                )
            }
        }
    }
}

@Composable
private fun EventEditorBottomBar(
    onPickPhoto: () -> Unit,
    onPickAttachment: () -> Unit,
    onPickSpeakers: () -> Unit,
    onPickLocation: () -> Unit,
) {
    NavigationBar(
        containerColor = EventEditorBar,
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onPickPhoto,
            icon = { Icon(Icons.Outlined.Image, contentDescription = stringResource(R.string.attachment_photo)) },
            label = { Text(stringResource(R.string.attachment_photo)) },
            alwaysShowLabel = false,
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickAttachment,
            icon = { Icon(Icons.Filled.AttachFile, contentDescription = stringResource(R.string.attachment_file)) },
            label = { Text(stringResource(R.string.attachment_file)) },
            alwaysShowLabel = false,
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickSpeakers,
            icon = { Icon(Icons.Outlined.People, contentDescription = stringResource(R.string.event_editor_speakers)) },
            label = { Text(stringResource(R.string.event_editor_speakers)) },
            alwaysShowLabel = false,
        )
        NavigationBarItem(
            selected = false,
            onClick = onPickLocation,
            icon = { Icon(Icons.Outlined.Place, contentDescription = stringResource(R.string.attachment_place)) },
            label = { Text(stringResource(R.string.attachment_place)) },
            alwaysShowLabel = false,
        )
    }
}

@Composable
private fun EventTypeOption(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = title,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

private fun EventType.toDisplayNameRes(): Int = when (this) {
    EventType.ONLINE -> R.string.event_type_online
    EventType.OFFLINE -> R.string.event_type_offline
}
