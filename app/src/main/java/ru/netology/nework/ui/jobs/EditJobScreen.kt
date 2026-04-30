package ru.netology.nework.ui.jobs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import ru.netology.nework.R
import ru.netology.nework.ui.theme.NeWorkColors
import ru.netology.nework.ui.theme.NeWorkFontWeights

private val JobEditorBg = NeWorkColors.ScreenBackground
private val JobEditorSurface = NeWorkColors.SurfacePrimary
private val JobEditorAccent = NeWorkColors.AccentPrimary
private val JobEditorBorder = NeWorkColors.BorderPrimary
private val JobEditorText = NeWorkColors.TextPrimary
private val JobEditorMuted = NeWorkColors.TextMuted
private val JobEditorDivider = NeWorkColors.DividerMedium

private val JobDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val JobDateParsers = listOf(
    DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    DateTimeFormatter.ISO_LOCAL_DATE,
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")),
)

private enum class JobDateField {
    START,
    FINISH,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJobScreen(
    navController: NavController,
    jobId: Long,
    viewModel: EditJobViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) {
        viewModel.load(jobId)
    }

    if (showDateDialog) {
        JobDatesDialog(
            start = state.start,
            finish = state.finish,
            onStartChange = viewModel::onStartChange,
            onFinishChange = viewModel::onFinishChange,
            onDismiss = { showDateDialog = false },
        )
    }

    Scaffold(
        containerColor = JobEditorBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (jobId == 0L) R.string.screen_job_new else R.string.screen_job_edit
                        ),
                        color = JobEditorText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = JobEditorText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JobEditorBg,
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JobEditorBg)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            JobEditorTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(R.string.job_label_name),
                trailingIcon = {
                    if (state.name.isNotBlank()) {
                        IconButton(onClick = { viewModel.onNameChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear_name),
                                tint = JobEditorText,
                            )
                        }
                    }
                },
            )

            JobEditorTextField(
                value = state.position,
                onValueChange = viewModel::onPositionChange,
                label = stringResource(R.string.job_label_position),
            )

            JobEditorTextField(
                value = state.link,
                onValueChange = viewModel::onLinkChange,
                label = stringResource(R.string.job_label_link),
            )

            OutlinedButton(
                onClick = { showDateDialog = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, JobEditorDivider),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = JobEditorText,
                ),
                contentPadding = ButtonDefaults.ContentPadding,
            ) {
                Text(
                    jobDateRangeLabel(
                        start = state.start,
                        finish = state.finish,
                        emptyLabel = stringResource(R.string.job_select_dates),
                        currentLabel = stringResource(R.string.job_now_short),
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.save {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = state.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JobEditorAccent,
                    contentColor = Color.White,
                    disabledContainerColor = JobEditorBorder,
                    disabledContentColor = Color.White,
                ),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    stringResource(
                        if (jobId == 0L) R.string.action_create else R.string.action_save_job
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun JobEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = JobEditorSurface,
            unfocusedContainerColor = JobEditorSurface,
            disabledContainerColor = JobEditorSurface,
            focusedIndicatorColor = JobEditorAccent,
            unfocusedIndicatorColor = JobEditorBorder,
            focusedLabelColor = JobEditorAccent,
            unfocusedLabelColor = JobEditorAccent,
            focusedTextColor = JobEditorText,
            unfocusedTextColor = JobEditorText,
            cursorColor = JobEditorAccent,
            focusedTrailingIconColor = JobEditorText,
            unfocusedTrailingIconColor = JobEditorText,
        ),
    )
}

@Composable
private fun JobDatesDialog(
    start: String,
    finish: String,
    onStartChange: (String) -> Unit,
    onFinishChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var activeField by remember { mutableStateOf<JobDateField?>(null) }

    if (activeField != null) {
        AndroidDatePicker(
            initialValue = when (activeField) {
                JobDateField.START -> start
                JobDateField.FINISH -> finish
                null -> ""
            },
            onDateSelected = { selectedDate ->
                when (activeField) {
                    JobDateField.START -> onStartChange(selectedDate)
                    JobDateField.FINISH -> onFinishChange(selectedDate)
                    null -> Unit
                }
            },
            onDismiss = { activeField = null },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = JobEditorSurface,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .padding(top = 18.dp, bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.job_dialog_select_date),
                    color = JobEditorMuted,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.job_enter_dates),
                        color = JobEditorText,
                        fontWeight = NeWorkFontWeights.Medium,
                    )
                    IconButton(onClick = { activeField = JobDateField.START }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.job_select_date),
                            tint = JobEditorMuted,
                        )
                    }
                }

                HorizontalDivider(color = JobEditorDivider)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    JobDateBox(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.job_date_label),
                        value = start,
                        placeholder = stringResource(R.string.job_date_placeholder),
                        onClick = { activeField = JobDateField.START },
                    )

                    JobDateBox(
                        modifier = Modifier.weight(1f),
                        label = null,
                        value = finish,
                        placeholder = stringResource(R.string.job_end_date_placeholder),
                        onClick = { activeField = JobDateField.FINISH },
                        onClear = {
                            if (finish.isNotBlank()) {
                                onFinishChange("")
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = JobEditorAccent,
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        enabled = start.isNotBlank(),
                    ) {
                        Text(
                            text = stringResource(R.string.action_done),
                            color = JobEditorAccent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JobDateBox(
    modifier: Modifier = Modifier,
    label: String?,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = JobEditorSurface,
        border = BorderStroke(1.dp, if (value.isBlank()) JobEditorDivider else JobEditorAccent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                label?.let {
                    Text(
                        text = it,
                        color = JobEditorAccent,
                    )
                }
                Text(
                    text = if (value.isBlank()) placeholder else displayJobDate(value),
                    color = if (value.isBlank()) JobEditorMuted else JobEditorText,
                )
            }

            if (onClear != null && value.isNotBlank()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_clear_finish_date),
                        tint = JobEditorMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun AndroidDatePicker(
    initialValue: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val initialDate = remember(initialValue) {
        parseJobDate(initialValue) ?: LocalDate.now()
    }

    DisposableEffect(context, initialDate) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(
                    LocalDate.of(year, month + 1, dayOfMonth).format(JobDateFormatter)
                )
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()

        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

private fun jobDateRangeLabel(
    start: String,
    finish: String,
    emptyLabel: String,
    currentLabel: String,
): String {
    if (start.isBlank()) return emptyLabel
    return "${
        displayJobDate(start)
    } – ${finish.takeIf { it.isNotBlank() }?.let(::displayJobDate) ?: currentLabel}"
}

private fun displayJobDate(value: String): String =
    parseJobDate(value)?.format(JobDateFormatter) ?: value

private fun parseJobDate(value: String): LocalDate? {
    val normalized = value.trim()
    if (normalized.isBlank()) return null

    runCatching { Instant.parse(normalized).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
        .getOrNull()
        ?.let { return it }
    runCatching { OffsetDateTime.parse(normalized).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
        .getOrNull()
        ?.let { return it }
    runCatching { ZonedDateTime.parse(normalized).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
        .getOrNull()
        ?.let { return it }

    return JobDateParsers.firstNotNullOfOrNull { formatter ->
        runCatching {
            LocalDate.parse(normalized, formatter)
        }.getOrNull()
    }
}
