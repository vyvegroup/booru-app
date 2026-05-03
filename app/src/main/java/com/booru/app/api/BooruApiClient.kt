package com.booru.app.api

import com.booru.app.model.BooruPost
import com.booru.app.model.GelbooruResponse
import com.booru.app.model.Rule34Response
import com.booru.app.util.TagHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Unified Booru API service that supports both Gelbooru and Rule34.xxx.
 */
class BooruApiClient {

    companion object {
        const val SOURCE_GELBOORU = "gelbooru"
        const val SOURCE_RULE34 = "rule34"

        private const val GELBOORU_BASE_URL = "https://gelbooru.com/"
        private const val RULE34_BASE_URL = "https://api.rule34.xxx/"

        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ============= Gelbooru Service =============

    interface GelbooruService {
        @GET("index.php")
        suspend fun getPosts(
            @Query("page") page: String = "dapi",
            @Query("s") s: String = "post",
            @Query("q") q: String = "index",
            @Query("json") json: Int = 1,
            @Query("api_key") apiKey: String,
            @Query("user_id") userId: String,
            @Query("tags") tags: String = "",
            @Query("limit") limit: Int = 40,
            @Query("pid") pid: Int = 0,
            @Query("rating") rating: String? = null
        ): GelbooruResponse
    }

    // ============= Rule34 Service =============

    interface Rule34Service {
        @GET("index.php")
        suspend fun getPosts(
            @Query("page") page: String = "dapi",
            @Query("s") s: String = "post",
            @Query("q") q: String = "index",
            @Query("json") json: Int = 1,
            @Query("api_key") apiKey: String,
            @Query("user_id") userId: String,
            @Query("tags") tags: String = "",
            @Query("limit") limit: Int = 40,
            @Query("pid") pid: Int = 0,
            @Query("rating") rating: String? = null
        ): Rule34Response
    }

    // ============= Client instances =============

    private val gelbooruRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GELBOORU_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val rule34Retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RULE34_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val gelbooruService: GelbooruService by lazy {
        gelbooruRetrofit.create(GelbooruService::class.java)
    }

    private val rule34Service: Rule34Service by lazy {
        rule34Retrofit.create(Rule34Service::class.java)
    }

    /**
     * Search posts from a specific source.
     *
     * @param source "gelbooru" or "rule34"
     * @param appTags App-format tags string (e.g., "Pokemon 1girl~2girls")
     * @param apiKey API key for the source
     * @param userId User ID for the source
     * @param page Page number (0-indexed)
     * @param limit Number of results per page
     * @return List of BooruPost
     */
    suspend fun searchPosts(
        source: String,
        appTags: String,
        apiKey: String,
        userId: String,
        page: Int = 0,
        limit: Int = 40
    ): List<BooruPost> {
        val formattedTags = TagHelper.convertForSource(appTags, source)

        return when (source.lowercase()) {
            SOURCE_RULE34 -> {
                val response = rule34Service.getPosts(
                    apiKey = apiKey,
                    userId = userId,
                    tags = formattedTags,
                    pid = page,
                    limit = limit
                )
                response.posts ?: emptyList()
            }
            SOURCE_GELBOORU -> {
                val response = gelbooruService.getPosts(
                    apiKey = apiKey,
                    userId = userId,
                    tags = formattedTags,
                    pid = page,
                    limit = limit
                )
                response.posts ?: emptyList()
            }
            else -> emptyList()
        }
    }

    /**
     * Get a list of recommended posts based on the tags of a given post (FYP system).
     *
     * @param source Active source
     * @param currentPost The post the user clicked on
     * @param apiKey API key
     * @param userId User ID
     * @param count Number of recommendations to fetch (default 5)
     * @return List of recommended BooruPost (excluding the current post)
     */
    suspend fun getFypRecommendations(
        source: String,
        currentPost: BooruPost,
        apiKey: String,
        userId: String,
        count: Int = 5
    ): List<BooruPost> {
        val tags = currentPost.tags ?: return emptyList()
        val topTags = TagHelper.extractTopTags(tags, count = 3)
        if (topTags.isEmpty()) return emptyList()

        // Fetch a larger batch, then filter out the current post
        val searchQuery = TagHelper.buildSearchQuery(topTags)
        val results = searchPosts(
            source = source,
            appTags = searchQuery,
            apiKey = apiKey,
            userId = userId,
            page = 0,
            limit = 20
        )

        return results
            .filter { it.id != currentPost.id }
            .shuffled()
            .take(count)
    }
}
