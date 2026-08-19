package cashvexa.expensetracker.budget.money.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ItemCurrencyBinding

data class CurrencyItem(val symbol: String, val code: String, val name: String)

class CurrencyAdapter(
    private val currencies: List<CurrencyItem>,
    var selectedCode: String,
    private var showFirstItemHint: Boolean = false,
    private val onItemClick: (CurrencyItem) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    private var lastPosition = -1

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
        
        val isSelected = (currency.code == selectedCode)
        holder.binding.rbSelected.isChecked = isSelected
        
        if (position == 0 && showFirstItemHint) {
            holder.binding.lottieHand.visibility = View.VISIBLE
            holder.binding.lottieHand.playAnimation()
            val pulseAnim = AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.anim_item_pulse)
            holder.binding.root.startAnimation(pulseAnim)
        } else {
            holder.binding.lottieHand.cancelAnimation()
            holder.binding.lottieHand.visibility = View.GONE
            if (position > lastPosition) {
                val anim = AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.item_animation_fall_down)
                holder.binding.root.startAnimation(anim)
                lastPosition = position
            } else {
                holder.binding.root.clearAnimation()
            }
        }

        holder.binding.root.setOnClickListener {
            val hadHint = showFirstItemHint
            showFirstItemHint = false
            val oldCode = selectedCode
            selectedCode = currency.code
            if (oldCode != selectedCode || hadHint) {
                notifyDataSetChanged()
            }
            onItemClick(currency)
        }
    }

    override fun onViewRecycled(holder: CurrencyViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.lottieHand.cancelAnimation()
        holder.binding.root.clearAnimation()
    }

    override fun getItemCount(): Int = currencies.size
}
