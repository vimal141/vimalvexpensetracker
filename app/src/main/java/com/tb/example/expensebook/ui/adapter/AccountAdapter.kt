package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.data.model.Account
import com.tb.example.expensebook.databinding.ItemAccountBinding

class AccountAdapter(
    private var currentAccountId: Int,
    private val onAccountClick: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<Account>, newCurrentAccountId: Int) {
        currentAccountId = newCurrentAccountId
        submitList(newList)
    }

    inner class AccountViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Account) {
            binding.tvAccountName.text = account.name
            val formattedNumber = account.accountNumber.chunked(4).joinToString(" ")
            binding.tvAccountNumber.text = formattedNumber
            
            binding.rbSelected.isChecked = account.id == currentAccountId

            binding.root.setOnClickListener {
                onAccountClick(account)
            }
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
