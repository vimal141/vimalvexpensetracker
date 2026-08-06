package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.Category
import com.tb.example.expensebook.databinding.ItemCategoryGridBinding
import com.tb.example.expensebook.utility.helper.CategoryHelper

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onSettingsClick: () -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Int = -1

    fun setSelectedCategory(categoryId: Int) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCategoryGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.txtCategoryName.text = category.name
            
            val iconRes = binding.root.context.resources.getIdentifier(
                category.iconName, "drawable", binding.root.context.packageName
            ).let { if (it != 0) it else R.drawable.ic_others }

            binding.imgCategory.setImageResource(iconRes)

            if (category.id == selectedCategoryId) {
                binding.iconContainer.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryColorSurface))
                binding.iconContainer.strokeColor = ContextCompat.getColor(binding.root.context, R.color.primaryColor)
                binding.imgCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.primaryColor))
                binding.txtCategoryName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primaryColor))
            } else {
                binding.iconContainer.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bgColorCard))
                binding.iconContainer.strokeColor = ContextCompat.getColor(binding.root.context, R.color.divider_color)
                binding.imgCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.subTextColor))
                binding.txtCategoryName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.subTextColor))
            }

            binding.root.setOnClickListener {
                if (category.id == -100) {
                    onSettingsClick()
                } else {
                    selectedCategoryId = category.id
                    onCategoryClick(category)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCategoryGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}
