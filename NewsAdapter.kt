package com.example.newsaggregatorapp


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsaggregatorapp.databinding.ItemNewsBinding

class NewsAdapter(
    private val onItemClicked: (Article) -> Unit,
    private val onFavorite: (Article) -> Unit,
    private val onMarkRead: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var articles: List<Article> = listOf()

    fun setNews(newArticles: List<Article>) {
        Log.d("NewsAdapter", "Setting news articles: ${newArticles.size}")
        articles = newArticles
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    fun markArticleAsRead(position: Int) {
        articles[position].let { article ->
            onMarkRead(article)
        }
        notifyItemChanged(position)
    }

    fun favoriteArticle(position: Int) {
        articles[position].let { article ->
            onFavorite(article)
        }
        notifyItemChanged(position)
    }

    inner class NewsViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.apply {
                tvTitle.text = article.title
                tvDescription.text = article.description

                // Load image using Glide
                Glide.with(itemView.context)
                    .load(article.image)
                    .into(ivThumbnail)

                // Set click listeners
                root.setOnClickListener { onItemClicked(article) }
                ivBookmark.setOnClickListener { onFavorite(article) }
            }
        }
    }
}
