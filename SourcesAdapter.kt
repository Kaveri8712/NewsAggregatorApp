package com.example.newsaggregatorapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsaggregatorapp.databinding.ItemSourceBinding

class SourcesAdapter(
    private val onSourceToggled: (String, Boolean) -> Unit
) : RecyclerView.Adapter<SourcesAdapter.SourceViewHolder>() {

    private val sources = mutableListOf<String>()
    private val selectedSources = mutableSetOf<String>()

    fun updateSources(newSources: List<String>) {
        sources.clear()
        sources.addAll(newSources)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val binding = ItemSourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        holder.bind(sources[position])
    }

    override fun getItemCount(): Int = sources.size

    inner class SourceViewHolder(private val binding: ItemSourceBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(source: String) {
            binding.checkboxSource.apply {
                text = source
                isChecked = selectedSources.contains(source)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSources.add(source)
                    } else {
                        selectedSources.remove(source)
                    }
                    onSourceToggled(source, isChecked)
                }
            }
        }
    }
} 