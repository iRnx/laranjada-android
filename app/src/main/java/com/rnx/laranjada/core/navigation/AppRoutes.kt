package com.rnx.laranjada.core.navigation

import android.net.Uri

object AppRoutes {

    object Home {
        const val route =
            "home"
    }

    object Favorites {
        const val route =
            "favorites"
    }

    object Search {
        const val route =
            "search"
    }

    object Account {
        const val route =
            "account"
    }

    object CreateProfile {
        const val route =
            "profile/create"
    }

    object EditProfiles {
        const val route =
            "profiles/edit"
    }

    object EditProfile {
        const val profileUuidArg =
            "profileUuid"

        const val route =
            "profiles/edit/{$profileUuidArg}"

        fun createRoute(
            profileUuid: String
        ): String {

            return "profiles/edit/" +
                    Uri.encode(
                        profileUuid
                    )
        }
    }

    object ProfilePin {
        const val profileUuidArg =
            "profileUuid"

        const val route =
            "profiles/pin/{$profileUuidArg}"

        fun createRoute(
            profileUuid: String
        ): String {

            return "profiles/pin/" +
                    Uri.encode(
                        profileUuid
                    )
        }
    }

    object AvatarPicker {
        const val profileUuidArg =
            "profileUuid"

        const val route =
            "profiles/avatar/{$profileUuidArg}"

        fun createRoute(
            profileUuid: String
        ): String {

            return "profiles/avatar/" +
                    Uri.encode(
                        profileUuid
                    )
        }
    }

    object DeleteProfile {
        const val profileUuidArg =
            "profileUuid"

        const val route =
            "profiles/delete/{$profileUuidArg}"

        fun createRoute(
            profileUuid: String
        ): String {

            return "profiles/delete/" +
                    Uri.encode(
                        profileUuid
                    )
        }
    }

    object Detail {
        const val contentTypeArg =
            "contentType"

        const val uuidArg =
            "uuid"

        const val route =
            "detail/{$contentTypeArg}/{$uuidArg}"

        fun createRoute(
            contentType: String,
            uuid: String
        ): String {

            return "detail/" +
                    "${Uri.encode(contentType)}/" +
                    Uri.encode(
                        uuid
                    )
        }
    }

    object Collection {
        const val uuidArg =
            "uuid"

        const val route =
            "collection/{$uuidArg}"

        fun createRoute(
            uuid: String
        ): String {

            return "collection/" +
                    Uri.encode(
                        uuid
                    )
        }
    }

    object MediaGrid {
        const val sectionSlugArg =
            "sectionSlug"

        const val titleArg =
            "title"

        const val route =
            "media-grid/{$sectionSlugArg}" +
                    "?$titleArg={$titleArg}"

        fun createRoute(
            sectionSlug: String,
            title: String
        ): String {

            return "media-grid/" +
                    Uri.encode(
                        sectionSlug
                    ) +
                    "?$titleArg=" +
                    Uri.encode(
                        title
                    )
        }
    }

    object Player {
        const val contentTypeArg =
            "contentType"

        const val uuidArg =
            "uuid"

        const val seriesUuidArg =
            "seriesUuid"

        const val initialPositionSecondsArg =
            "initialPositionSeconds"

        const val route =
            "player/{$contentTypeArg}/{$uuidArg}" +
                    "?$seriesUuidArg={$seriesUuidArg}" +
                    "&$initialPositionSecondsArg={$initialPositionSecondsArg}"

        fun createRoute(
            contentType: String,
            uuid: String,
            seriesUuid: String = "",
            initialPositionSeconds: Long = 0L
        ): String {

            val safePosition =
                initialPositionSeconds
                    .coerceAtLeast(
                        0L
                    )

            return "player/" +
                    "${Uri.encode(contentType)}/" +
                    "${Uri.encode(uuid)}" +
                    "?$seriesUuidArg=" +
                    Uri.encode(
                        seriesUuid
                    ) +
                    "&$initialPositionSecondsArg=" +
                    safePosition
        }
    }
}