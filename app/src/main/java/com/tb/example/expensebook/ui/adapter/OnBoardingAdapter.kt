package com.tb.example.expensebook.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tb.example.expensebook.data.model.OnBoardingItem
import com.tb.example.expensebook.R

class OnBoardingAdapter(private val items: List<OnBoardingItem>) :
    RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {
        return OnBoardingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false))
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OnBoardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivOnboarding: ImageView = view.findViewById(R.id.ivOnboarding)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        fun bind(item: OnBoardingItem) {
            ivOnboarding.setImageResource(item.image)
            tvTitle.text = item.title
            tvDescription.text = item.description
        }
    }
}