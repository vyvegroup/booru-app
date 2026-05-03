package com.booru.app.model

import com.google.gson.annotations.SerializedName

data class BooruPost(
    @SerializedName("id") val id: Int,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("md5") val md5: String? = null,
    @SerializedName("directory") val directory: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("change") val change: Int? = null,
    @SerializedName("owner") val owner: String? = null,
    @SerializedName("creator_id") val creatorId: Int? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("sample") val sample: Int? = null,
    @SerializedName("preview_height") val previewHeight: Int? = null,
    @SerializedName("preview_width") val previewWidth: Int? = null,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("has_notes") val hasNotes: String? = null,
    @SerializedName("has_comments") val hasComments: String? = null,
    @SerializedName("file_url") val fileUrl: String? = null,
    @SerializedName("preview_url") val previewUrl: String? = null,
    @SerializedName("sample_url") val sampleUrl: String? = null,
    @SerializedName("sample_height") val sampleHeight: Int? = null,
    @SerializedName("sample_width") val sampleWidth: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("post_locked") val postLocked: Int? = null,
    @SerializedName("has_children") val hasChildren: String? = null
) {
    /**
     * Returns the effective image URL, preferring file_url then constructing from directory/image
     */
    fun getEffectiveImageUrl(baseUrl: String): String {
        if (!fileUrl.isNullOrBlank()) return fileUrl
        if (!image.isNullOrBlank() && !directory.isNullOrBlank()) {
            return "$baseUrl/images/$directory/$image"
        }
        return ""
    }

    /**
     * Returns the effective preview URL
     */
    fun getEffectivePreviewUrl(baseUrl: String): String {
        if (!previewUrl.isNullOrBlank()) return previewUrl
        if (!directory.isNullOrBlank() && !image.isNullOrBlank()) {
            val previewName = image.substringBeforeLast('.') + "_preview." + image.substringAfterLast('.')
            return "$baseUrl/thumbnails/$directory/thumbnail_$previewName"
        }
        return getEffectiveImageUrl(baseUrl)
    }

    /**
     * Returns the effective sample URL
     */
    fun getEffectiveSampleUrl(baseUrl: String): String {
        if (!sampleUrl.isNullOrBlank()) return sampleUrl
        return getEffectiveImageUrl(baseUrl)
    }

    fun getTagList(): List<String> {
        return tags?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    }
}

/**
 * Gelbooru API response wrapper
 */
data class GelbooruResponse(
    @SerializedName("post") val posts: List<BooruPost>? = null,
    @SerializedName("@attributes")
    val attributes: ResponseAttributes? = null
) {
    data class ResponseAttributes(
        @SerializedName("limit") val limit: Int? = null,
        @SerializedName("offset") val offset: Int? = null,
        @SerializedName("count") val count: Int? = null
    )
}

/**
 * Rule34 API can return either a list directly or an object with a posts array
 */
data class Rule34Response(
    @SerializedName("posts") val posts: List<BooruPost>? = null
)
