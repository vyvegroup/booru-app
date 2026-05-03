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
 * Horizontal RecyclerView adapter for FYP (For You Page) recommendations.
 * Displays 5 suggested artworks in a horizontal scrollable list.
 */
class FypAdapter(
    private val context: Context,
    private val onClick: (BooruPost) -> Unit
) : RecyclerView.Adapter<FypAdapter.FypViewHolder>() {

    private val recommendations = mutableListOf<BooruPost>()

    fun submitList(items: List<BooruPost>) {
        recommendations.clear()
        recommendations.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FypViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fyp, parent, false)
        return FypViewHolder(view)
    }

    override fun onBindViewHolder(holder: FypViewHolder, position: Int) {
        val post = recommendations[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = recommendations.size

    inner class FypViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivFypImage)
        private val tvFypScore: TextView = itemView.findViewById(R.id.tvFypScore)
        private val tvFypTags: TextView = itemView.findViewById(R.id.tvFypTags)

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

            tvFypScore.text = "Score: ${post.score ?: 0}"

            // Show first 3 tags
            val tagList = post.getTagList().take(3)
            tvFypTags.text = tagList.joinToString(", ")

            itemView.setOnClickListener { onClick(post) }
        }
    }
}
