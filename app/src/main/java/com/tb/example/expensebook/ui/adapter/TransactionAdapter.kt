package com.tb.example.expensebook.ui.adapter

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.utility.helper.CategoryHelper
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.Transaction
import com.tb.example.expensebook.databinding.ItemTransactionBinding
import com.tb.example.expensebook.utility.Constants
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var searchQuery: String = "",
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    var isSelectionMode = false
    val selectedItems = mutableSetOf<Transaction>()
    var onSelectionChanged: ((Boolean, Int) -> Unit)? = null

    private val dateFormatCurrentYear = SimpleDateFormat("dd MMM", Locale.US)
    private val dateFormatOtherYear = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private var lastPosition = -1

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val symbol = PrefManager.currencySymbol

            binding.txtCategoryName.text = getHighlightedText(transaction.category, searchQuery)
            
            val cal = Calendar.getInstance()
            cal.timeInMillis = transaction.date
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (cal.get(Calendar.YEAR) == currentYear) {
                binding.txtDate.text = dateFormatCurrentYear.format(cal.time)
            } else {
                binding.txtDate.text = dateFormatOtherYear.format(cal.time)
            }
            
            if (transaction.notes.isNullOrBlank()) {
                binding.txtNotes.text = binding.root.context.getString(R.string.text_not_applicable)
            } else {
                binding.txtNotes.text = getHighlightedText(transaction.notes, searchQuery)
            }

            if (transaction.type.trim().equals(Constants.TYPE_INCOME, ignoreCase = true)) {
                binding.txtAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.income_green))
                val prefix = "+"
                binding.txtAmount.text = "$prefix$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(transaction.amount)}"
            } else {
                binding.txtAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.expense_red))
                val prefix = "-"
                binding.txtAmount.text = "$prefix$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(transaction.amount)}"
            }

            val bgColor = CategoryHelper.getBgColor(binding.root.context, transaction.category)
            binding.iconContainer.setCardBackgroundColor(bgColor)

            if (isSelectionMode && selectedItems.contains(transaction)) {
                binding.cardTransaction.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryColorSurface))
                binding.imgCategory.setImageResource(R.drawable.ic_check)
                binding.imgCategory.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.primaryColor),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.cardTransaction.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bgColorCard))
                val (iconRes, _) = CategoryHelper.getIconAndColor(transaction.category)
                binding.imgCategory.setImageResource(iconRes)
                binding.imgCategory.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.imgCategory.setColorFilter(
                    CategoryHelper.getColor(binding.root.context, transaction.category),
                    PorterDuff.Mode.SRC_IN
                )
            }
            
            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(transaction)
                } else {
                    onItemClick(transaction)
                }
            }
            
            binding.root.setOnLongClickListener {
                if (!isSelectionMode && onSelectionChanged != null) {
                    isSelectionMode = true
                    toggleSelection(transaction)
                }
                true
            }
            
            if (!isSelectionMode && bindingAdapterPosition > lastPosition) {
                val anim = AnimationUtils.loadAnimation(binding.root.context, R.anim.item_animation_fall_down)
                binding.root.startAnimation(anim)
                lastPosition = bindingAdapterPosition
            }
        }
        
        private fun getHighlightedText(text: String, query: String): CharSequence {
            if (query.isBlank()) return text
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase()
            var start = lowerText.indexOf(lowerQuery)
            if (start < 0) return text

            val spannable = SpannableString(text)
            val color = ContextCompat.getColor(binding.root.context, R.color.primaryColorSurface)

            while (start >= 0) {
                val end = start + query.length
                spannable.setSpan(
                    BackgroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                start = lowerText.indexOf(lowerQuery, end)
            }
            return spannable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<Transaction>, newQuery: String = "") {
        lastPosition = -1
        searchQuery = newQuery
        submitList(newList)
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    fun toggleSelection(transaction: Transaction) {
        if (selectedItems.contains(transaction)) {
            selectedItems.remove(transaction)
        } else {
            selectedItems.add(transaction)
        }
        if (selectedItems.isEmpty()) {
            isSelectionMode = false
        }
        onSelectionChanged?.invoke(isSelectionMode, selectedItems.size)
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedItems.addAll(currentList)
        onSelectionChanged?.invoke(isSelectionMode, selectedItems.size)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        onSelectionChanged?.invoke(isSelectionMode, selectedItems.size)
        notifyDataSetChanged()
    }
}
