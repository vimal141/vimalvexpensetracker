package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.databinding.ItemCurrencyBinding

data class CurrencyItem(val symbol: String, val code: String, val name: String)

class CurrencyAdapter(
    private val currencies: List<CurrencyItem>,
    var selectedCode: String
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    inner class CurrencyViewHolder(val binding: ItemCurrencyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        
        holder.binding.txtCurrencySymbol.text = currency.symbol
        holder.binding.txtCurrencyCode.text = currency.code
        holder.binding.txtCurrencyName.text = currency.name
        
        if (position == currencies.size - 1) {
            holder.binding.divider.visibility = View.GONE
        } else {
            holder.binding.divider.visibility = View.VISIBLE
        }

        if (currency.code == selectedCode) {
            holder.binding.icCheck.visibility = View.VISIBLE
        } else {
            holder.binding.icCheck.visibility = View.GONE
        }

        holder.binding.root.setOnClickListener {
            val oldCode = selectedCode
            selectedCode = currency.code
            if (oldCode != selectedCode) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = currencies.size
}
