package ru.netology.nework.ui.events

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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.AppBottomBar
import ru.netology.nework.ui.common.AuthAwareTopBar
import ru.netology.nework.ui.common.AuthRequiredDialog
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.auth.AuthViewModel

private val EventsScreenBg = Color(0xFFF7F2FA)
private val EventsAccent = Color(0xFF6F52B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }

    if (showAuthDialog) {
        AuthRequiredDialog(
            title = "Нужен аккаунт",
            message = "Чтобы создать событие, войди в аккаунт или зарегистрируйся.",
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

    Scaffold(
        containerColor = EventsScreenBg,
        topBar = {
            AuthAwareTopBar(
                navController = navController,
                isAuthorized = authState.authorized,
                onLogout = authViewModel::logout,
                title = "События",
                containerColor = EventsScreenBg,
                contentColor = EventsAccent,
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (authState.authorized) {
                        navController.navigate(Destination.EventEditor.createRoute())
                    } else {
                        showAuthDialog = true
                    }
                },
                containerColor = Color(0xFFE7D9F5),
                contentColor = EventsAccent,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить событие",
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingState()
            state.error != null -> {
                ErrorState(onRetry = { viewModel.load() })
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = state.data,
                        key = { it.id },
                    ) { event ->
                        EventCard(
                            event = event,
                            onClick = {
                                navController.navigate(
                                    Destination.EventDetails.createRoute(event.id)
                                )
                            },
                            onLike = { viewModel.onLike(event) },
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${event.author}\n\n${event.content}")
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, "Поделиться событием")
                                )
                            },
                            onEdit = {
                                navController.navigate(Destination.EventEditor.createRoute(event.id))
                            },
                            onDelete = { viewModel.onDelete(event.id) },
                        )
                    }
                }
            }
        }
    }
}
