package com.rnx.laranjada.data.mapper

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.core.network.MediaUrlResolver
import com.rnx.laranjada.feature.home.MediaItemUi
import org.json.JSONObject

object RelatedContentMapper {

    fun fromJson(json: JSONObject): List<MediaItemUi> {
        val results = json.optJSONArray("results") ?: return emptyList()

        return buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue

                val uuid = item.optString("uuid").orEmpty()
                val contentType = item.optString("content_type").orEmpty()

                if (uuid.isBlank() || contentType.isBlank()) {
                    continue
                }

                val year = item.optString("year").orEmpty()
                val rating = item.optString("rating").orEmpty()

                val subtitle = buildString {
                    if (year.isNotBlank()) append(year)

                    if (rating.isNotBlank() && rating != "0" && rating != "0.0") {
                        if (isNotBlank()) append(" • ")
                        append(rating)
                    }
                }

                add(
                    MediaItemUi(
                        title = item.optString("title").orEmpty(),
                        subtitle = subtitle,
                        imageUrl = MediaUrlResolver.resolve(
                            item.optString("image_url").orEmpty()
                        ),
                        progress = null,
                        gradientColors = listOf(
                            Color(0xFF151515),
                            Color(0xFF050505)
                        ),
                        uuid = uuid,
                        contentType = contentType
                    )
                )
            }
        }
    }
}