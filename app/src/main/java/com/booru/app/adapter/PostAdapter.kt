package com.booru.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.booru.app.R
import com.booru.app.model.BooruPost
import com.booru.app.util.Prefs

/**
 * RecyclerView adapter for displaying booru posts in a grid.
 */
class PostAdapter(
    private val context: Context,
    private val onClick: (BooruPost) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<BooruPost>()

    fun submitList(newPosts: List<BooruPost>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun addPosts(newPosts: List<BooruPost>) {
        val startPos = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(startPos, newPosts.size)
    }

    fun clear() {
        posts.clear()
        notifyDataSetChanged()
    }

    fun getItems(): List<BooruPost> = posts.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivPostPreview)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)

        fun bind(post: BooruPost) {
            val source = Prefs.getActiveSource(context)
            val baseUrl = if (source == "rule34") "https://api.rule34.xxx" else "https://gelbooru.com"

            val previewUrl = post.getEffectivePreviewUrl(baseUrl)
            if (previewUrl.isNotBlank()) {
                imageView.load(previewUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_placeholder)
                    error(R.drawable.ic_placeholder)
                }
            }

            tvScore.text = post.score?.toString() ?: ""
            itemView.setOnClickListener { onClick(post) }
        }
    }
}
