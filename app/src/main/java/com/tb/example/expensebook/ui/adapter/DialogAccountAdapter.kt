package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.data.model.Account
import com.tb.example.expensebook.databinding.ItemDialogAccountBinding

class DialogAccountAdapter(
    var currentAccountId: Int
) : ListAdapter<Account, DialogAccountAdapter.ViewHolder>(DialogAccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDialogAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<Account>, newCurrentAccountId: Int) {
        currentAccountId = newCurrentAccountId
        submitList(newList)
    }

    private fun changeSelection(newAccountId: Int) {
        val oldAccountId = currentAccountId
        if (oldAccountId != newAccountId) {
            currentAccountId = newAccountId
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val binding: ItemDialogAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Account) {
            binding.tvAccountName.text = account.name
            
            val accNum = account.accountNumber
            val maskedNumber = if (accNum.length > 4) {
                val mask = "X".repeat(accNum.length - 4)
                val chunkedMask = mask.chunked(4).joinToString(" ")
                "$chunkedMask ${accNum.takeLast(4)}"
            } else {
                accNum
            }
            binding.tvAccountNumber.text = maskedNumber
            
            binding.rbSelected.isChecked = account.id == currentAccountId

            binding.root.setOnClickListener {
                changeSelection(account.id)
            }
            binding.rbSelected.setOnClickListener {
                changeSelection(account.id)
            }
        }
    }

    private class DialogAccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
