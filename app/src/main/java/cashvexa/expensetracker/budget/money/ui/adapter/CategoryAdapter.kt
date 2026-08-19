package cashvexa.expensetracker.budget.money.ui.adapter

import cashvexa.expensetracker.budget.money.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cashvexa.expensetracker.budget.money.data.model.Category
import cashvexa.expensetracker.budget.money.utility.helper.CategoryHelper
import cashvexa.expensetracker.budget.money.databinding.ItemCategoryGridBinding

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
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
                binding.iconContainer.setCardBackgroundColor(CategoryHelper.getColor(binding.root.context, category.name))
                binding.iconContainer.strokeColor = CategoryHelper.getColor(binding.root.context, category.name)
                binding.imgCategory.setColorFilter(ContextCompat.getColor(binding.root.context, android.R.color.white))
                binding.txtCategoryName.setTextColor(CategoryHelper.getColor(binding.root.context, category.name))
            } else {
                binding.iconContainer.setCardBackgroundColor(CategoryHelper.getBgColor(binding.root.context, category.name))
                binding.iconContainer.strokeColor = ContextCompat.getColor(binding.root.context, R.color.divider_color)
                binding.imgCategory.setColorFilter(CategoryHelper.getColor(binding.root.context, category.name))
                binding.txtCategoryName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.TextColor))
            }

            binding.root.setOnClickListener {
                selectedCategoryId = category.id
                onCategoryClick(category)
                notifyDataSetChanged()
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
