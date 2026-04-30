package ru.netology.nework.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ru.netology.nework.R
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
    val postsLabel = stringResource(R.string.nav_posts)
    val eventsLabel = stringResource(R.string.nav_events)
    val usersLabel = stringResource(R.string.nav_users)

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Destination.Posts.route,
            onClick = {
                navController.navigate(Destination.Posts.route) {
                    popUpTo(Destination.Posts.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = postsLabel) },
            label = { Text(postsLabel) },
        )

        NavigationBarItem(
            selected = currentRoute == Destination.Events.route,
            onClick = {
                navController.navigate(Destination.Events.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Event, contentDescription = eventsLabel) },
            label = { Text(eventsLabel) },
        )

        NavigationBarItem(
            selected = usersSelected,
            onClick = {
                navController.navigate(Destination.Users.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.People, contentDescription = usersLabel) },
            label = { Text(usersLabel) },
        )
    }
}
