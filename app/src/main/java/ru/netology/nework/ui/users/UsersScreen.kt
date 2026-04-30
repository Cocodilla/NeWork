package ru.netology.nework.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.netology.nework.R
import ru.netology.nework.navigation.Destination
import ru.netology.nework.ui.common.AppBottomBar
import ru.netology.nework.ui.common.AuthAwareTopBar
import ru.netology.nework.ui.common.ErrorState
import ru.netology.nework.ui.common.LoadingState
import ru.netology.nework.ui.auth.AuthViewModel
import ru.netology.nework.ui.theme.NeWorkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    navController: NavController,
    viewModel: UsersViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NeWorkColors.ScreenBackground,
        topBar = {
            AuthAwareTopBar(
                navController = navController,
                isAuthorized = authState.authorized,
                onLogout = authViewModel::logout,
                title = stringResource(R.string.screen_users),
                containerColor = NeWorkColors.ScreenBackground,
                contentColor = NeWorkColors.AccentPrimary,
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { paddingValues ->
        when {
            state.loading -> LoadingState()

            state.error != null -> ErrorState(onRetry = viewModel::load)

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.data, key = { it.id }) { user ->
                        UserListCard(
                            user = user,
                            onClick = {
                                navController.navigate(Destination.UserProfile.createRoute(user.id))
                            },
                        )
                    }
                }
            }
        }
    }
}
