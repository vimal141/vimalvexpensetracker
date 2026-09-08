package cashvexa.expensetracker.budget.money.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import cashvexa.expensetracker.budget.money.data.model.LanguageModel
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val languages: List<LanguageModel>,
    var selectedCode: String,
    private var showFirstItemHint: Boolean = false,
    private val onItemClick: (LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var lastPosition = -1

    class LanguageViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = languages[position]

        holder.binding.imgFlag.setImageResource(item.flagRes)
        holder.binding.txtLanguageName.text = item.name

        if (item.subTitle.isNotEmpty()) {
            holder.binding.txtSubTitle.visibility = View.VISIBLE
            holder.binding.txtSubTitle.text = item.subTitle
        } else {
            holder.binding.txtSubTitle.visibility = View.GONE
        }

        val isSelected = (item.code == selectedCode)
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
            selectedCode = item.code
            if (oldCode != selectedCode || hadHint) {
                notifyDataSetChanged()
            }
            onItemClick(item)
        }
    }

    override fun onViewRecycled(holder: LanguageViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.lottieHand.cancelAnimation()
        holder.binding.root.clearAnimation()
    }

    override fun getItemCount(): Int = languages.size
}
