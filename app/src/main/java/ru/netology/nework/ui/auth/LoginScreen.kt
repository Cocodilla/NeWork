package ru.netology.nework.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.loginState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AuthEvent.Authorized -> navController.completeAuthFlow()
                AuthEvent.WrongCredentials -> {
                    Toast.makeText(
                        context,
                        "Неправильный логин или пароль",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                AuthEvent.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Не удалось выполнить вход",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                AuthEvent.LoggedOut,
                AuthEvent.UserAlreadyExists,
                -> Unit
            }
        }
    }

    AuthScreenBackground {
        Scaffold(
            containerColor = AuthUi.ScreenBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Логин") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
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
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                OutlinedTextField(
                    value = state.login,
                    onValueChange = viewModel::changeLogin,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.loginError != null,
                    label = { Text("Логин") },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.loginError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(error)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::changePassword,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.passwordError != null,
                    label = { Text("Пароль") },
                    visualTransformation = if (passwordVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
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
                                    "Скрыть пароль"
                                } else {
                                    "Показать пароль"
                                },
                            )
                        }
                    },
                    colors = authTextFieldColors(),
                    shape = AuthUi.FieldShape,
                )

                state.passwordError?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    AuthErrorText(error)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = viewModel::signIn,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AuthUi.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthUi.PrimaryButton,
                        contentColor = AuthUi.PrimaryButtonText,
                        disabledContainerColor = Color(0xFFD9D9D9),
                        disabledContentColor = Color(0xFF8C8C8C),
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Text(if (state.loading) "Входим..." else "Войти")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { navController.navigate(Destination.Register.route) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Нет аккаунта? Зарегистрироваться",
                        color = AuthUi.SecondaryText,
                    )
                }
            }
        }
    }
}
