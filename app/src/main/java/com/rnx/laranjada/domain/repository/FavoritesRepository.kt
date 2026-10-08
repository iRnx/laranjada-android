
package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.FavoriteStatus
import com.rnx.laranjada.domain.model.FavoritesPage
import com.rnx.laranjada.domain.model.FavoritesQuery

interface FavoritesRepository {

    suspend fun list(
        query: FavoritesQuery
    ): FavoritesPage

    suspend fun status(
        contentType: String,
        contentUuid: String
    ): FavoriteStatus

    suspend fun toggle(
        contentType: String,
        contentUuid: String
    ): FavoriteStatus
}
