package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.data.model.LanguageModel
import com.tb.example.expensebook.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val languages: List<LanguageModel>,
    var selectedCode: String,
    private val onItemClick: (LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var lastPosition = -1

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
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

        holder.binding.root.setOnClickListener {
            val oldCode = selectedCode
            selectedCode = item.code
            if (oldCode != selectedCode) {
                notifyDataSetChanged()
            }
            onItemClick(item)
        }

        if (position > lastPosition) {
            val anim = android.view.animation.AnimationUtils.loadAnimation(holder.binding.root.context, com.tb.example.expensebook.R.anim.item_animation_fall_down)
            holder.binding.root.startAnimation(anim)
            lastPosition = position
        }
    }

    override fun getItemCount(): Int = languages.size
}
