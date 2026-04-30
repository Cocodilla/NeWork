package ru.netology.nework.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ru.netology.nework.R
import ru.netology.nework.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthAwareTopBar(
    navController: NavController,
    isAuthorized: Boolean,
    onLogout: () -> Unit,
    title: String = "NeWork",
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val profileDescription = stringResource(R.string.cd_profile)
    val logoutDescription = stringResource(R.string.cd_logout)
    val loginLabel = stringResource(R.string.action_login)
    val registerLabel = stringResource(R.string.action_register)

    TopAppBar(
        title = {
            Text(text = title)
        },
        actions = {
            if (isAuthorized) {
                IconButton(
                    onClick = {
                        navController.navigate(Destination.MyProfile.route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = profileDescription,
                    )
                }

                IconButton(
                    onClick = onLogout
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = logoutDescription,
                    )
                }
            } else {
                TextButton(
                    onClick = {
                        navController.navigate(Destination.Login.route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(loginLabel, color = contentColor)
                }

                TextButton(
                    onClick = {
                        navController.navigate(Destination.Register.route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(registerLabel, color = contentColor)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor,
        ),
    )
}
