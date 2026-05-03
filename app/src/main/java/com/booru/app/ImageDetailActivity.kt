package com.booru.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.booru.app.adapter.FypAdapter
import com.booru.app.api.BooruApiClient
import com.booru.app.model.BooruPost
import com.booru.app.util.Prefs
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var fypAdapter: FypAdapter
    private val apiClient = BooruApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail"

        val postId = intent.getIntExtra("post_id", 0)
        val tags = intent.getStringExtra("post_tags") ?: ""
        val score = intent.getIntExtra("post_score", 0)
        val fileUrl = intent.getStringExtra("post_file_url") ?: ""
        val sampleUrl = intent.getStringExtra("post_sample_url") ?: ""
        val previewUrl = intent.getStringExtra("post_preview_url") ?: ""
        val image = intent.getStringExtra("post_image") ?: ""
        val directory = intent.getStringExtra("post_directory") ?: ""
        val width = intent.getIntExtra("post_width", 0)
        val height = intent.getIntExtra("post_height", 0)
        val rating = intent.getStringExtra("post_rating") ?: ""
        val source = intent.getStringExtra("post_source") ?: ""
        val owner = intent.getStringExtra("post_owner") ?: ""
        val created = intent.getStringExtra("post_created") ?: ""

        // Build the current post object
        val currentPost = BooruPost(
            id = postId,
            tags = tags,
            score = score,
            fileUrl = fileUrl.ifBlank { null },
            sampleUrl = sampleUrl.ifBlank { null },
            previewUrl = previewUrl.ifBlank { null },
            image = image.ifBlank { null },
            directory = directory.ifBlank { null },
            width = if (width > 0) width else null,
            height = if (height > 0) height else null,
            rating = rating.ifBlank { null },
            source = source.ifBlank { null },
            owner = owner.ifBlank { null },
            createdAt = created.ifBlank { null }
        )

        // Load the sample image
        val ivImage = findViewById<ImageView>(R.id.ivDetailImage)
        val activeSource = Prefs.getActiveSource(this)
        val baseUrl = if (activeSource == "rule34") "https://api.rule34.xxx" else "https://gelbooru.com"

        val displayUrl = if (sampleUrl.isNotBlank()) sampleUrl
            else if (fileUrl.isNotBlank()) fileUrl
            else currentPost.getEffectiveImageUrl(baseUrl)

        if (displayUrl.isNotBlank()) {
            ivImage.load(displayUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
            }
        }

        // Set info text
        findViewById<TextView>(R.id.tvDetailId).text = "ID: $postId"
        findViewById<TextView>(R.id.tvDetailScore).text = "Score: $score"
        findViewById<TextView>(R.id.tvDetailSize).text = if (width > 0 && height > 0) "$width × $height" else "N/A"
        findViewById<TextView>(R.id.tvDetailRating).text = "Rating: ${rating.uppercase()}"
        findViewById<TextView>(R.id.tvDetailSource).text = "Source: ${source.ifBlank { "N/A" }}"
        findViewById<TextView>(R.id.tvDetailOwner).text = "By: ${owner.ifBlank { "Unknown" }}"
        findViewById<TextView>(R.id.tvDetailCreated).text = if (created.isNotBlank()) "Created: $created" else ""

        // Tags chips
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupTags)
        val tagList = tags.split(" ").filter { it.isNotBlank() }.take(20)
        for (tag in tagList) {
            val chip = Chip(this).apply {
                text = tag
                isClickable = true
                textSize = 12f
                chipMinHeight = 32f
                setOnClickListener {
                    // Copy tag to clipboard or use for search
                    android.content.ClipData.newPlainText("tag", tag).let { clip ->
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            ?.let { it as android.content.ClipboardManager }
                            ?.setPrimaryClip(clip)
                    }
                    Toast.makeText(this@ImageDetailActivity, "Tag '$tag' copied", Toast.LENGTH_SHORT).show()
                }
            }
            chipGroup.addView(chip)
        }

        // FYP Section - Show 5 recommendations
        val fypRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewFyp)
        val fypTitle = findViewById<TextView>(R.id.tvFypTitle)
        val fypProgress = findViewById<View>(R.id.progressFyp)

        fypAdapter = FypAdapter(this) { recommendedPost ->
            // Open the recommended post detail
            val intent = android.content.Intent(this, ImageDetailActivity::class.java)
            intent.putExtra("post_id", recommendedPost.id)
            intent.putExtra("post_tags", recommendedPost.tags)
            intent.putExtra("post_score", recommendedPost.score ?: 0)
            intent.putExtra("post_file_url", recommendedPost.fileUrl ?: "")
            intent.putExtra("post_sample_url", recommendedPost.sampleUrl ?: "")
            intent.putExtra("post_preview_url", recommendedPost.previewUrl ?: "")
            intent.putExtra("post_image", recommendedPost.image ?: "")
            intent.putExtra("post_directory", recommendedPost.directory ?: "")
            intent.putExtra("post_width", recommendedPost.width ?: 0)
            intent.putExtra("post_height", recommendedPost.height ?: 0)
            intent.putExtra("post_rating", recommendedPost.rating ?: "")
            intent.putExtra("post_source", recommendedPost.source ?: "")
            intent.putExtra("post_owner", recommendedPost.owner ?: "")
            intent.putExtra("post_created", recommendedPost.createdAt ?: "")
            startActivity(intent)
        }
        fypRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        fypRecyclerView.adapter = fypAdapter

        // Load FYP recommendations
        loadFypRecommendations(currentPost, fypTitle, fypProgress)
    }

    private fun loadFypRecommendations(
        currentPost: BooruPost,
        fypTitle: TextView,
        fypProgress: View
    ) {
        val source = Prefs.getActiveSource(this)
        val apiKey = if (source == "gelbooru") {
            Prefs.getGelbooruApiKey(this)
        } else {
            Prefs.getRule34ApiKey(this)
        }
        val userId = if (source == "gelbooru") {
            Prefs.getGelbooruUserId(this)
        } else {
            Prefs.getRule34UserId(this)
        }

        if (apiKey.isBlank() || userId.isBlank()) {
            fypTitle.text = "Recommended (Configure API in Settings)"
            fypProgress.visibility = View.GONE
            return
        }

        fypProgress.visibility = View.VISIBLE
        fypTitle.text = "You might also like..."

        lifecycleScope.launch {
            try {
                val recommendations = apiClient.getFypRecommendations(
                    source = source,
                    currentPost = currentPost,
                    apiKey = apiKey,
                    userId = userId,
                    count = 5
                )

                fypProgress.visibility = View.GONE
                if (recommendations.isNotEmpty()) {
                    fypTitle.text = "You might also like..."
                    fypAdapter.submitList(recommendations)
                    findViewById<View>(R.id.layoutFyp).visibility = View.VISIBLE
                } else {
                    fypTitle.text = "No recommendations found"
                }
            } catch (e: Exception) {
                fypProgress.visibility = View.GONE
                fypTitle.text = "Could not load recommendations"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
