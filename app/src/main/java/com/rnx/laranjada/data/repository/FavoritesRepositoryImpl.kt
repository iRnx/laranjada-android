
package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.FavoritesMapper
import com.rnx.laranjada.data.remote.api.FavoritesApiService
import com.rnx.laranjada.domain.model.FavoriteStatus
import com.rnx.laranjada.domain.model.FavoritesPage
import com.rnx.laranjada.domain.model.FavoritesQuery
import com.rnx.laranjada.domain.repository.FavoritesRepository

class FavoritesRepositoryImpl(
    private val api: FavoritesApiService =
        FavoritesApiService
) : FavoritesRepository {

    override suspend fun list(
        query: FavoritesQuery
    ): FavoritesPage {

        val response =
            api.list(
                query
            )

        return FavoritesMapper.page(
            response
        )
    }

    override suspend fun status(
        contentType: String,
        contentUuid: String
    ): FavoriteStatus {

        val response =
            api.status(
                contentType = contentType,
                contentUuid = contentUuid
            )

        return FavoritesMapper.status(
            response
        )
    }

    override suspend fun toggle(
        contentType: String,
        contentUuid: String
    ): FavoriteStatus {

        val response =
            api.toggle(
                contentType = contentType,
                contentUuid = contentUuid
            )

        return FavoritesMapper.status(
            response
        )
    }
}
