package com.tb.example.expensebook.utility.helper

import android.content.Context
import androidx.core.content.ContextCompat
import com.tb.example.expensebook.R
import java.util.Locale

object CategoryHelper {

    fun getIconAndColor(category: String): Pair<Int, Int> {
        return when (category.lowercase(Locale.US).trim()) {
            "food", "dining" -> Pair(R.drawable.ic_food, R.color.category_food_bg)
            "shopping", "clothing" -> Pair(R.drawable.ic_shopping, R.color.category_shopping_bg)
            "transport", "fuel", "gas", "car" -> Pair(R.drawable.ic_transport, R.color.category_transport_bg)
            "bills", "utility", "rent" -> Pair(R.drawable.ic_bills, R.color.category_bills_bg)
            "entertainment" -> Pair(R.drawable.ic_entertainment, R.color.category_entertainment_bg)
            "health" -> Pair(R.drawable.ic_health, R.color.category_health_bg)
            "education" -> Pair(R.drawable.ic_education, R.color.category_education_bg)
            "travel", "trip" -> Pair(R.drawable.ic_travel, R.color.category_travel_bg)
            "salary" -> Pair(R.drawable.ic_salary, R.color.category_salary_bg)
            "business" -> Pair(R.drawable.ic_business, R.color.category_business_bg)
            "freelance" -> Pair(R.drawable.ic_freelance, R.color.category_freelance_bg)
            "investment" -> Pair(R.drawable.ic_investment, R.color.category_investment_bg)
            else -> Pair(R.drawable.ic_others, R.color.category_others_bg)
        }
    }

    fun getColor(context: Context, category: String): Int {
        val colorRes = when (category.lowercase(Locale.US).trim()) {
            "food", "dining" -> R.color.category_food
            "shopping", "clothing" -> R.color.category_shopping
            "transport", "fuel", "gas", "car" -> R.color.category_transport
            "bills", "utility", "rent" -> R.color.category_bills
            "entertainment" -> R.color.category_entertainment
            "health" -> R.color.category_health
            "education" -> R.color.category_education
            "travel", "trip" -> R.color.category_travel
            "salary" -> R.color.category_salary
            "business" -> R.color.category_business
            "freelance" -> R.color.category_freelance
            "investment" -> R.color.category_investment
            else -> R.color.category_others
        }
        return ContextCompat.getColor(context, colorRes)
    }

    fun getBgColor(context: Context, category: String): Int {
        val colorRes = when (category.lowercase(Locale.US).trim()) {
            "food", "dining" -> R.color.category_food_bg
            "shopping", "clothing" -> R.color.category_shopping_bg
            "transport", "fuel", "gas", "car" -> R.color.category_transport_bg
            "bills", "utility", "rent" -> R.color.category_bills_bg
            "entertainment" -> R.color.category_entertainment_bg
            "health" -> R.color.category_health_bg
            "education" -> R.color.category_education_bg
            "travel", "trip" -> R.color.category_travel_bg
            "salary" -> R.color.category_salary_bg
            "business" -> R.color.category_business_bg
            "freelance" -> R.color.category_freelance_bg
            "investment" -> R.color.category_investment_bg
            else -> R.color.category_others_bg
        }
        return ContextCompat.getColor(context, colorRes)
    }
}
