package com.rnx.laranjada.core.navigation

import android.net.Uri

object AppRoutes {
    object Home {
        const val route = "home"
    }

    object Detail {
        const val contentTypeArg = "contentType"
        const val uuidArg = "uuid"

        const val route = "detail/{$contentTypeArg}/{$uuidArg}"

        fun createRoute(
            contentType: String,
            uuid: String
        ): String {
            return "detail/${Uri.encode(contentType)}/${Uri.encode(uuid)}"
        }
    }
}