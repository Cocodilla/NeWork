package ru.netology.nework.ui.auth

import androidx.navigation.NavController
import ru.netology.nework.navigation.Destination

private val authRoutes = setOf(
    Destination.Login.route,
    Destination.Register.route,
)

fun NavController.completeAuthFlow() {
    while (currentBackStackEntry?.destination?.route in authRoutes) {
        if (!popBackStack()) {
            break
        }
    }

    if (currentBackStackEntry?.destination?.route in authRoutes || currentBackStackEntry == null) {
        navigate(Destination.Posts.route) {
            launchSingleTop = true
            popUpTo(graph.startDestinationId) {
                inclusive = false
            }
        }
    }
}
