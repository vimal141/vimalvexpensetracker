package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.Category
import com.tb.example.expensebook.databinding.ItemCategoryManageBinding

class ManageCategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, ManageCategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    inner class ViewHolder(private val binding: ItemCategoryManageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.txtCategoryName.text = category.name
            
            val iconRes = binding.root.context.resources.getIdentifier(
                category.iconName, "drawable", binding.root.context.packageName
            ).let { if (it != 0) it else R.drawable.ic_others }

            binding.imgCategory.setImageResource(iconRes)
            
            binding.btnEdit.setOnClickListener { onEditClick(category) }
            binding.btnDelete.setOnClickListener { onDeleteClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCategoryManageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}
