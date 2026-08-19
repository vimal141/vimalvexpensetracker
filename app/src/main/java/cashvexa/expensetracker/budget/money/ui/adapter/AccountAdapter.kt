package cashvexa.expensetracker.budget.money.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cashvexa.expensetracker.budget.money.data.model.Account
import cashvexa.expensetracker.budget.money.databinding.ItemAccountBinding

class AccountAdapter(
    var selectedAccountId: Int,
    private val onAccountClick: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    fun updateSelectedAccountId(newAccountId: Int) {
        if (selectedAccountId != newAccountId) {
            selectedAccountId = newAccountId
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Account) {
            binding.tvAccountName.text = account.name
            val formattedNumber = account.accountNumber.chunked(4).joinToString(" ")
            binding.tvAccountNumber.text = formattedNumber

            binding.rbSelected.isChecked = account.id == selectedAccountId

            binding.root.setOnClickListener {
                updateSelectedAccountId(account.id)
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
