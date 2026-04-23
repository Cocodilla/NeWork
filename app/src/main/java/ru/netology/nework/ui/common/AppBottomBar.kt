package ru.netology.nework.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.netology.nework.navigation.Destination

@Composable
fun AppBottomBar(
    navController: NavController,
) {
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val usersSelected = currentRoute == Destination.Users.route ||
        currentRoute == Destination.UserProfile.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Destination.Posts.route,
            onClick = {
                navController.navigate(Destination.Posts.route) {
                    popUpTo(Destination.Posts.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Article, contentDescription = "Posts") },
            label = { Text("Posts") },
        )

        NavigationBarItem(
            selected = currentRoute == Destination.Events.route,
            onClick = {
                navController.navigate(Destination.Events.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Event, contentDescription = "Events") },
            label = { Text("Events") },
        )

        NavigationBarItem(
            selected = usersSelected,
            onClick = {
                navController.navigate(Destination.Users.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.People, contentDescription = "Users") },
            label = { Text("Users") },
        )
    }
}
