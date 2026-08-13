package com.rnx.laranjada.core.navigation

import android.net.Uri

object AppRoutes {

    object Home {
        const val route = "home"
    }

    object Account {
        const val route = "account"
    }

    object Detail {
        const val contentTypeArg = "contentType"
        const val uuidArg = "uuid"

        const val route =
            "detail/{$contentTypeArg}/{$uuidArg}"

        fun createRoute(
            contentType: String,
            uuid: String
        ): String {
            return "detail/${Uri.encode(contentType)}/${Uri.encode(uuid)}"
        }
    }

    object Collection {
        const val uuidArg = "uuid"

        const val route =
            "collection/{$uuidArg}"

        fun createRoute(
            uuid: String
        ): String {
            return "collection/${Uri.encode(uuid)}"
        }
    }

    object MediaGrid {
        const val sectionSlugArg = "sectionSlug"
        const val titleArg = "title"

        const val route =
            "media-grid/{$sectionSlugArg}?$titleArg={$titleArg}"

        fun createRoute(
            sectionSlug: String,
            title: String
        ): String {
            return "media-grid/${Uri.encode(sectionSlug)}" +
                    "?$titleArg=${Uri.encode(title)}"
        }
    }

    object Player {
        const val contentTypeArg = "contentType"
        const val uuidArg = "uuid"
        const val hlsUrlArg = "hlsUrl"
        const val seriesUuidArg = "seriesUuid"

        const val route =
            "player/{$contentTypeArg}/{$uuidArg}" +
                    "?$hlsUrlArg={$hlsUrlArg}" +
                    "&$seriesUuidArg={$seriesUuidArg}"

        fun createRoute(
            contentType: String,
            uuid: String,
            hlsUrl: String,
            seriesUuid: String = ""
        ): String {
            return "player/${Uri.encode(contentType)}/${Uri.encode(uuid)}" +
                    "?$hlsUrlArg=${Uri.encode(hlsUrl)}" +
                    "&$seriesUuidArg=${Uri.encode(seriesUuid)}"
        }
    }
}