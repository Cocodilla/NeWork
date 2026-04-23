package ru.netology.nework.navigation

import ru.netology.nework.model.Coordinates

sealed class Destination(val route: String) {
    data object Posts : Destination("posts")
    data object Events : Destination("events")
    data object Users : Destination("users")
    data object UserProfile : Destination("user_profile/{userId}") {
        fun createRoute(userId: Long): String = "user_profile/$userId"
    }
    data object Login : Destination("login")
    data object Register : Destination("register")
    data object MyProfile : Destination("my_profile")
    data object JobEditor : Destination("job_editor?jobId={jobId}") {
        private const val baseRoute = "job_editor"

        fun createRoute(jobId: Long = 0L): String =
            if (jobId == 0L) baseRoute else "$baseRoute?jobId=$jobId"
    }

    data object EventEditor : Destination("event_editor?eventId={eventId}") {
        private const val baseRoute = "event_editor"

        fun createRoute(eventId: Long = 0L): String =
            if (eventId == 0L) baseRoute else "$baseRoute?eventId=$eventId"
    }

    data object EventDetails : Destination("event_details/{eventId}") {
        fun createRoute(eventId: Long): String = "event_details/$eventId"
    }

    data object LocationPicker : Destination("location_picker/{resultKey}?lat={lat}&lng={lng}") {
        fun createRoute(
            resultKey: String,
            initialCoordinates: Coordinates? = null,
        ): String {
            val lat = initialCoordinates?.lat?.toString().orEmpty()
            val lng = initialCoordinates?.lng?.toString().orEmpty()
            return "location_picker/$resultKey?lat=$lat&lng=$lng"
        }
    }

    data object PostEditor : Destination("post_editor?postId={postId}") {
        private const val baseRoute = "post_editor"

        fun createRoute(postId: Long = 0L): String =
            if (postId == 0L) baseRoute else "$baseRoute?postId=$postId"
    }

    data object PostDetails : Destination("post_details/{postId}") {
        fun createRoute(postId: Long): String = "post_details/$postId"
    }

    data object Likers : Destination("likers/{postId}") {
        fun createRoute(postId: Long): String = "likers/$postId"
    }
}
