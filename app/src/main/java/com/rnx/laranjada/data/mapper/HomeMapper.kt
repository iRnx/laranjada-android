package com.rnx.laranjada.data.mapper

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.feature.home.CategoryUi
import com.rnx.laranjada.feature.home.CollectionUi
import com.rnx.laranjada.feature.home.ContinueWatchingUi
import com.rnx.laranjada.feature.home.HeroBannerUi
import com.rnx.laranjada.feature.home.HomeContentSectionUi
import com.rnx.laranjada.feature.home.HomeUiState
import com.rnx.laranjada.feature.home.MediaItemUi
import org.json.JSONArray
import org.json.JSONObject

object HomeMapper {

    fun fromJson(json: JSONObject): HomeUiState {
        return HomeUiState(
            heroBanners = mapBanners(json.optJSONArray("banners")),
            selectedHeroIndex = 0,
            categories = mapCategories(json.optJSONArray("categories")),
            selectedCategoryIndex = 0,
            continueWatching = mapContinueWatching(json.optJSONArray("continue_watching")),
            contentSections = mapSections(json.optJSONArray("sections")),
            collections = mapCollections(json.optJSONArray("collections"))
        )
    }

    private fun mapBanners(array: JSONArray?): List<HeroBannerUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                val contentUuid = item.optNullableString("content_uuid")
                val contentType = item.optNullableString("content_type")

                if (contentUuid.isNullOrBlank() || contentType.isNullOrBlank()) {
                    continue
                }

                add(
                    HeroBannerUi(
                        title = item.optString("title"),
                        year = "",
                        rating = "",
                        genres = emptyList(),
                        duration = "",
                        quality = "",
                        synopsis = "",
                        imageUrl = item.optNullableString("image_mobile_url")
                            ?: item.optNullableString("image_url")
                            ?: "",
                        gradientColors = defaultGradient(index),
                        uuid = contentUuid,
                        contentType = contentType
                    )
                )
            }
        }
    }

    private fun mapCategories(array: JSONArray?): List<CategoryUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                add(
                    CategoryUi(
                        name = item.optString("title")
                    )
                )
            }
        }
    }

    private fun mapContinueWatching(array: JSONArray?): List<ContinueWatchingUi> {
        if (array == null) return emptyList()

        return emptyList()
    }

    private fun mapSections(array: JSONArray?): List<HomeContentSectionUi> {
        if (array == null) return emptyList()

        return buildList {
            for (sectionIndex in 0 until array.length()) {
                val section = array.optJSONObject(sectionIndex) ?: continue
                val contentType = section.optNullableString("content_type") ?: ""
                val itemsArray = section.optJSONArray("items")

                val items = mapMediaItems(
                    array = itemsArray,
                    fallbackContentType = contentType,
                    sectionIndex = sectionIndex
                )

                if (items.isEmpty()) {
                    continue
                }

                add(
                    HomeContentSectionUi(
                        title = section.optString("title"),
                        contentType = contentType,
                        items = items
                    )
                )
            }
        }
    }

    private fun mapMediaItems(
        array: JSONArray?,
        fallbackContentType: String,
        sectionIndex: Int
    ): List<MediaItemUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val uuid = item.optNullableString("uuid") ?: continue

                add(
                    MediaItemUi(
                        title = item.optString("title"),
                        subtitle = item.optNullableString("original_title") ?: "",
                        imageUrl = item.optNullableString("image_url") ?: "",
                        progress = null,
                        gradientColors = defaultGradient(sectionIndex + index),
                        uuid = uuid,
                        contentType = item.optNullableString("content_type") ?: fallbackContentType
                    )
                )
            }
        }
    }

    private fun mapCollections(array: JSONArray?): List<CollectionUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                val uuid = item.optNullableString("uuid") ?: continue

                add(
                    CollectionUi(
                        uuid = uuid,
                        title = item.optString("title"),
                        imageUrl = item.optNullableString("image_url") ?: "",
                        gradientColors = defaultGradient(index)
                    )
                )
            }
        }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null

        val value = optString(name).trim()

        return value.ifBlank { null }
    }

    private fun defaultGradient(index: Int): List<Color> {
        val gradients = listOf(
            listOf(Color(0xFF08121A), Color(0xFF0A0A0B)),
            listOf(Color(0xFF1E3A8A), Color(0xFF020617)),
            listOf(Color(0xFF7F1D1D), Color(0xFF111827)),
            listOf(Color(0xFF14532D), Color(0xFF020617)),
            listOf(Color(0xFF4C1D95), Color(0xFF111827)),
            listOf(Color(0xFF713F12), Color(0xFF111827))
        )

        return gradients[index % gradients.size]
    }
}