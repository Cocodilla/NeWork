package ru.netology.nework.ui.auth

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.util.toTempFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.registerState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var repeatPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var photoSelectionError by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        val photoError = validateAvatar(context, uri)
        if (photoError != null) {
            photoSelectionError = photoError
            viewModel.changePhoto(PhotoModel())
            return@rememberLauncherForActivityResult
        }

        val tempFile = runCatching { uri.toTempFile(context) }.getOrElse {
            photoSelectionError = context.getString(R.string.error_prepare_image)
            viewModel.changePhoto(PhotoModel())
            return@rememberLauncherForActivityResult
        }

        photoSelectionError = null
        viewModel.changePhoto(
            PhotoModel(
                uri = uri,
                path = tempFile.absolutePath,
            )
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AuthEvent.Authorized -> navController.completeAuthFlow()
                AuthEvent.UserAlreadyExists -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_user_exists),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                AuthEvent.UnknownError -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_register_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                AuthEvent.LoggedOut,
                AuthEvent.WrongCredentials,
                -> Unit
            }
        }
    }

    AuthScreenBackground {
        Scaffold(
            containerColor = AuthUi.ScreenBackground,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.screen_register)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AuthUi.AppBarBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                if (state.photo.uri == null) {
                    AuthAvatarPlaceholder(
                        modifier = Modifier
                            .size(124.dp)
                            .clickable { galleryLauncher.launch("image/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.cd_photo),
                            modifier = Modifier.size(56.dp),
                            tint = Color.Black,
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(state.photo.uri),
                        contentDescription = stringResource(R.string.cd_selected_photo),
                        modifier = Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AuthUi.PrimaryButton,
                        contentColor = Color.White,
                    ),
                    shape = AuthUi.ButtonShape,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.action_pick_photo))
                }

                photoSelectionError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthErrorText(error)
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = state.login,
                    onValueChange = viewModel::changeRegisterLogin,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.loginError != null,
                    label = { Text(stringResource(R.string.label_login)) },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.loginError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(stringResource(error))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::changeRegisterName,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.nameError != null,
                    label = { Text(stringResource(R.string.label_name)) },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.nameError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(stringResource(error))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::changeRegisterPassword,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.passwordError != null,
                    label = { Text(stringResource(R.string.label_password)) },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = if (passwordVisible) {
                                    stringResource(R.string.cd_hide_password)
                                } else {
                                    stringResource(R.string.cd_show_password)
                                },
                            )
                        }
                    },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.passwordError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(stringResource(error))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.repeatPassword,
                    onValueChange = viewModel::changeRepeatPassword,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.repeatPasswordError != null,
                    label = { Text(stringResource(R.string.label_repeat_password)) },
                    visualTransformation = if (repeatPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                            Icon(
                                imageVector = if (repeatPasswordVisible) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = if (repeatPasswordVisible) {
                                    stringResource(R.string.cd_hide_password)
                                } else {
                                    stringResource(R.string.cd_show_password)
                                },
                            )
                        }
                    },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.repeatPasswordError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(stringResource(error))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = viewModel::register,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AuthUi.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthUi.PrimaryButton,
                        contentColor = AuthUi.PrimaryButtonText,
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Text(
                        if (state.loading) {
                            stringResource(R.string.action_registering)
                        } else {
                            stringResource(R.string.action_register_submit)
                        }
                    )
                }
            }
        }
    }
}

private fun validateAvatar(
    context: Context,
    uri: Uri,
): String? {
    val mimeType = context.contentResolver.getType(uri)?.lowercase()
    if (mimeType != "image/jpeg" && mimeType != "image/png") {
        return context.getString(R.string.error_avatar_format)
    }

    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    context.contentResolver.openInputStream(uri).use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return context.getString(R.string.error_image_read)
    }

    if (bounds.outWidth > 2048 || bounds.outHeight > 2048) {
        return context.getString(R.string.error_avatar_max_size)
    }

    return null
}
