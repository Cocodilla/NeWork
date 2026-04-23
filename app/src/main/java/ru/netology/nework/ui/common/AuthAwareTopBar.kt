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
import androidx.navigation.NavController
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
                        contentDescription = "Profile",
                    )
                }

                IconButton(
                    onClick = onLogout
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
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
                    Text("Вход", color = contentColor)
                }

                TextButton(
                    onClick = {
                        navController.navigate(Destination.Register.route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Регистрация", color = contentColor)
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
