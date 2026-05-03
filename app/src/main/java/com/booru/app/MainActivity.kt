package com.booru.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booru.app.adapter.PostAdapter
import com.booru.app.api.BooruApiClient
import com.booru.app.model.BooruPost
import com.booru.app.util.Prefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private val apiClient = BooruApiClient()

    private var currentPage = 0
    private var isLoading = false
    private var currentQuery = ""
    private var hasMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.title = getString(R.string.app_name)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = PostAdapter(this) { post ->
            openDetail(post)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Pagination
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isLoading && hasMore) {
                    loadPosts(currentQuery, currentPage + 1)
                }
            }
        })

        // Pull to refresh
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            currentPage = 0
            hasMore = true
            adapter.clear()
            loadPosts(currentQuery, 0)
            swipeRefreshLayout.isRefreshing = false
        }

        // Search button
        findViewById<View>(R.id.btnSearch).setOnClickListener {
            performSearch()
        }

        // Source toggle button
        findViewById<View>(R.id.btnToggleSource).setOnClickListener {
            toggleSource()
        }

        updateSourceButtonText()

        // Check if API is configured
        checkApiConfig()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSourceButtonText()
    }

    private fun toggleSource() {
        val current = Prefs.getActiveSource(this)
        val newSource = if (current == "gelbooru") "rule34" else "gelbooru"
        Prefs.setActiveSource(this, newSource)
        updateSourceButtonText()

        // Reload if there's a current query
        if (currentQuery.isNotBlank()) {
            currentPage = 0
            hasMore = true
            adapter.clear()
            loadPosts(currentQuery, 0)
        }
    }

    private fun updateSourceButtonText() {
        val btn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleSource)
        val source = Prefs.getActiveSource(this)
        val label = if (source == "gelbooru") "Gelbooru" else "Rule34"
        btn.text = "Source: $label"
    }

    private fun performSearch() {
        val etSearch = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        val query = etSearch?.text?.toString()?.trim() ?: ""

        if (query.isBlank()) {
            Toast.makeText(this, "Enter search tags", Toast.LENGTH_SHORT).show()
            return
        }

        checkApiAndSearch(query)
    }

    private fun checkApiAndSearch(query: String) {
        val source = Prefs.getActiveSource(this)
        val isConfigured = if (source == "gelbooru") {
            Prefs.isGelbooruConfigured(this)
        } else {
            Prefs.isRule34Configured(this)
        }

        if (!isConfigured) {
            MaterialAlertDialogBuilder(this)
                .setTitle("API Not Configured")
                .setMessage("Please set up your $source API credentials in Settings first.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        currentQuery = query
        currentPage = 0
        hasMore = true
        adapter.clear()
        Prefs.setLastSearch(this, query)
        loadPosts(query, 0)
    }

    private fun checkApiConfig() {
        val lastSearch = Prefs.getLastSearch(this)
        if (lastSearch.isNotBlank()) {
            val etSearch = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
            etSearch?.setText(lastSearch)
        }

        val gelbooruOk = Prefs.isGelbooruConfigured(this)
        val rule34Ok = Prefs.isRule34Configured(this)

        if (!gelbooruOk && !rule34Ok) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Welcome to BooruApp")
                .setMessage("To get started, please configure your API credentials in Settings.\n\n" +
                        "You need a User ID and API Key from Gelbooru and/or Rule34.xxx.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun loadPosts(query: String, page: Int) {
        if (isLoading) return
        isLoading = true

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

        lifecycleScope.launch {
            try {
                val results = apiClient.searchPosts(
                    source = source,
                    appTags = query,
                    apiKey = apiKey,
                    userId = userId,
                    page = page,
                    limit = 40
                )

                if (results.isEmpty()) {
                    hasMore = false
                    if (page == 0) {
                        Toast.makeText(this@MainActivity, "No results found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    currentPage = page
                    if (page == 0) {
                        adapter.submitList(results)
                    } else {
                        adapter.addPosts(results)
                    }
                    // If we got fewer results than requested, assume no more pages
                    if (results.size < 40) {
                        hasMore = false
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun openDetail(post: BooruPost) {
        val intent = Intent(this, ImageDetailActivity::class.java)
        intent.putExtra("post_id", post.id)
        intent.putExtra("post_tags", post.tags)
        intent.putExtra("post_score", post.score ?: 0)
        intent.putExtra("post_file_url", post.fileUrl ?: "")
        intent.putExtra("post_sample_url", post.sampleUrl ?: "")
        intent.putExtra("post_preview_url", post.previewUrl ?: "")
        intent.putExtra("post_image", post.image ?: "")
        intent.putExtra("post_directory", post.directory ?: "")
        intent.putExtra("post_width", post.width ?: 0)
        intent.putExtra("post_height", post.height ?: 0)
        intent.putExtra("post_rating", post.rating ?: "")
        intent.putExtra("post_source", post.source ?: "")
        intent.putExtra("post_owner", post.owner ?: "")
        intent.putExtra("post_created", post.createdAt ?: "")
        startActivity(intent)
    }
}
