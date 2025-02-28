package com.example.newsaggregatorapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsaggregatorapp.databinding.ItemBookmarkBinding

class BookmarksAdapter(
    private val onItemClicked: (BookmarkedArticle) -> Unit,
    private val onRemoveBookmark: (BookmarkedArticle) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    private val bookmarks = mutableListOf<BookmarkedArticle>()

    fun setBookmarks(newBookmarks: List<BookmarkedArticle>) {
        bookmarks.clear()
        bookmarks.addAll(newBookmarks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    override fun getItemCount(): Int = bookmarks.size

    inner class BookmarkViewHolder(private val binding: ItemBookmarkBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(article: BookmarkedArticle) {
            binding.tvTitle.text = article.title
            binding.tvDescription.text = article.description

            article.urlToImage?.let { imageUrl ->
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .into(binding.ivThumbnail)
            }

            binding.root.setOnClickListener { onItemClicked(article) }
            binding.btnRemove.setOnClickListener { onRemoveBookmark(article) }
        }
    }
} 