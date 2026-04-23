package ru.netology.nework.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.netology.nework.ui.auth.LoginScreen
import ru.netology.nework.ui.auth.RegisterScreen
import ru.netology.nework.ui.events.EditEventScreen
import ru.netology.nework.ui.events.EventDetailsScreen
import ru.netology.nework.ui.events.EventsScreen
import ru.netology.nework.ui.jobs.EditJobScreen
import ru.netology.nework.ui.posts.EditPostScreen
import ru.netology.nework.ui.posts.LikersScreen
import ru.netology.nework.ui.posts.PostDetailsScreen
import ru.netology.nework.ui.posts.PostsScreen
import ru.netology.nework.ui.map.PickLocationScreen
import ru.netology.nework.ui.profile.ProfileScreen
import ru.netology.nework.ui.users.UserProfileScreen
import ru.netology.nework.ui.users.UsersScreen

@Composable
fun NeWorkNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.Posts.route,
    ) {
        composable(Destination.Posts.route) {
            PostsScreen(navController = navController)
        }

        composable(Destination.Events.route) {
            EventsScreen(navController = navController)
        }

        composable(
            route = Destination.EventEditor.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            EditEventScreen(
                navController = navController,
                eventId = eventId,
            )
        }

        composable(
            route = Destination.EventDetails.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            EventDetailsScreen(
                navController = navController,
                eventId = eventId,
            )
        }

        composable(Destination.Users.route) {
            UsersScreen(navController = navController)
        }

        composable(
            route = Destination.UserProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            UserProfileScreen(
                navController = navController,
                userId = userId,
            )
        }

        composable(Destination.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Destination.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(Destination.MyProfile.route) {
            ProfileScreen(navController = navController)
        }

        composable(
            route = Destination.JobEditor.route,
            arguments = listOf(
                navArgument("jobId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong("jobId") ?: 0L
            EditJobScreen(
                navController = navController,
                jobId = jobId,
            )
        }

        composable(
            route = Destination.PostEditor.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            EditPostScreen(
                navController = navController,
                postId = postId,
            )
        }

        composable(
            route = Destination.LocationPicker.route,
            arguments = listOf(
                navArgument("resultKey") { type = NavType.StringType },
                navArgument("lat") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("lng") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val resultKey = backStackEntry.arguments?.getString("resultKey").orEmpty()
            val lat = backStackEntry.arguments?.getString("lat").orEmpty()
            val lng = backStackEntry.arguments?.getString("lng").orEmpty()
            PickLocationScreen(
                navController = navController,
                resultKey = resultKey,
                initialLocation = "$lat, $lng",
            )
        }

        composable(
            route = Destination.PostDetails.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            PostDetailsScreen(
                navController = navController,
                postId = postId,
            )
        }

        composable(
            route = Destination.Likers.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            LikersScreen(
                navController = navController,
                postId = postId,
            )
        }
    }
}
