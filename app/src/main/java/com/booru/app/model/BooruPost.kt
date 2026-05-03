package com.booru.app.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

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
    fun getEffectiveImageUrl(baseUrl: String): String {
        if (!fileUrl.isNullOrBlank()) return fileUrl
        if (!image.isNullOrBlank() && !directory.isNullOrBlank()) {
            return "$baseUrl/images/$directory/$image"
        }
        return ""
    }

    fun getEffectivePreviewUrl(baseUrl: String): String {
        if (!previewUrl.isNullOrBlank()) return previewUrl
        if (!directory.isNullOrBlank() && !image.isNullOrBlank()) {
            val previewName = image.substringBeforeLast('.') + "_preview." + image.substringAfterLast('.')
            return "$baseUrl/thumbnails/$directory/thumbnail_$previewName"
        }
        return getEffectiveImageUrl(baseUrl)
    }

    fun getEffectiveSampleUrl(baseUrl: String): String {
        if (!sampleUrl.isNullOrBlank()) return sampleUrl
        return getEffectiveImageUrl(baseUrl)
    }

    fun getTagList(): List<String> {
        return tags?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    }
}

/**
 * Flexible JSON deserializer that handles both:
 * - Direct array: [{"id":1, ...}, {"id":2, ...}]
 * - Object with array: {"post": [...]} or {"posts": [...]}
 *
 * This is needed because Gelbooru/Rule34 APIs may return either format
 * depending on the endpoint and parameters.
 */
class BooruPostListDeserializer : JsonDeserializer<List<BooruPost>> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<BooruPost> {
        val postType = object : TypeToken<List<BooruPost>>() {}.type

        return when {
            // Case 1: JSON array directly: [{"id":1,...}, ...]
            json.isJsonArray -> {
                context.deserialize<List<BooruPost>>(json, postType)
            }
            // Case 2: JSON object with "post" key (Gelbooru XML-style JSON)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                when {
                    obj.has("post") -> {
                        val postElement = obj.get("post")
                        if (postElement.isJsonArray) {
                            context.deserialize<List<BooruPost>>(postElement, postType)
                        } else {
                            emptyList()
                        }
                    }
                    obj.has("posts") -> {
                        val postsElement = obj.get("posts")
                        if (postsElement.isJsonArray) {
                            context.deserialize<List<BooruPost>>(postsElement, postType)
                        } else {
                            emptyList()
                        }
                    }
                    // If object looks like a single post (has "id" field), wrap it
                    obj.has("id") -> {
                        listOf(context.deserialize<BooruPost>(json, BooruPost::class.java))
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
