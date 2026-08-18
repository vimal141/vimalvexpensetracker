package com.fintrack.expensetracker.utility.helper

import android.content.Context
import androidx.core.content.ContextCompat
import com.fintrack.expensetracker.R
import java.util.Locale

object CategoryHelper {

    fun getIconAndColor(category: String): Pair<Int, Int> {
        val cat = category.lowercase(Locale.US).trim()
        val iconRes = when (cat) {
            "food", "dining" -> R.drawable.ic_food
            "shopping" -> R.drawable.ic_shopping
            "clothing" -> R.drawable.ic_clothing
            "transportation", "transport", "fuel", "gas" -> R.drawable.ic_transport
            "car" -> R.drawable.ic_car
            "bills", "utility", "rent" -> R.drawable.ic_bills
            "phone" -> R.drawable.ic_phone
            "home emi", "housing", "home" -> R.drawable.ic_home
            "entertainment" -> R.drawable.ic_entertainment
            "health" -> R.drawable.ic_health
            "education" -> R.drawable.ic_education
            "travel", "trip" -> R.drawable.ic_travel
            "beauty" -> R.drawable.ic_beauty
            "sports" -> R.drawable.ic_sports
            "social" -> R.drawable.ic_social
            "alcohol" -> R.drawable.ic_alcohol
            "cigarettes" -> R.drawable.ic_cigarette
            "electronics" -> R.drawable.ic_electronics
            "pets" -> R.drawable.ic_pets
            "repairs" -> R.drawable.ic_repairs
            "gifts" -> R.drawable.ic_gift
            "donations" -> R.drawable.ic_donation
            "lottery" -> R.drawable.ic_lottery
            "snacks" -> R.drawable.ic_snacks
            "kids" -> R.drawable.ic_kids
            "vegetables" -> R.drawable.ic_vegetables
            "fruits" -> R.drawable.ic_fruits
            "old money" -> R.drawable.ic_round_rupay
            "salary" -> R.drawable.ic_salary
            "bonus" -> R.drawable.ic_bonus
            "business" -> R.drawable.ic_business
            "freelance", "part-time" -> R.drawable.ic_freelance
            "investment", "investments" -> R.drawable.ic_investment
            "bank transfer" -> R.drawable.ic_bank_transfer
            "wallet" -> R.drawable.ic_wallet
            else -> R.drawable.ic_others
        }
        val bgRes = getBgColorRes(cat)
        return Pair(iconRes, bgRes)
    }

    fun getColor(context: Context, category: String): Int {
        val colorRes = getColorRes(category.lowercase(Locale.US).trim())
        return ContextCompat.getColor(context, colorRes)
    }

    fun getBgColor(context: Context, category: String): Int {
        val colorRes = getBgColorRes(category.lowercase(Locale.US).trim())
        return ContextCompat.getColor(context, colorRes)
    }

    private fun getColorRes(cat: String): Int {
        return when (cat) {
            "food", "dining" -> R.color.category_food
            "shopping" -> R.color.category_shopping
            "clothing" -> R.color.category_clothing
            "transportation", "transport", "fuel", "gas" -> R.color.category_transport
            "car" -> R.color.category_car
            "bills", "utility", "rent" -> R.color.category_bills
            "phone" -> R.color.category_phone
            "home emi", "housing", "home" -> R.color.category_home_emi
            "entertainment" -> R.color.category_entertainment
            "health" -> R.color.category_health
            "education" -> R.color.category_education
            "travel", "trip" -> R.color.category_travel
            "beauty" -> R.color.category_beauty
            "sports" -> R.color.category_sports
            "social" -> R.color.category_social
            "alcohol" -> R.color.category_alcohol
            "cigarettes" -> R.color.category_cigarettes
            "electronics" -> R.color.category_electronics
            "pets" -> R.color.category_pets
            "repairs" -> R.color.category_repairs
            "gifts" -> R.color.category_gifts
            "donations" -> R.color.category_donations
            "lottery" -> R.color.category_lottery
            "snacks" -> R.color.category_snacks
            "kids" -> R.color.category_kids
            "vegetables" -> R.color.category_vegetables
            "fruits" -> R.color.category_fruits
            "old money" -> R.color.category_old_money
            "salary" -> R.color.category_salary
            "bonus" -> R.color.category_bonus
            "business" -> R.color.category_business
            "freelance", "part-time" -> R.color.category_freelance
            "investment", "investments" -> R.color.category_investment
            "bank transfer" -> R.color.category_bank_transfer
            "wallet" -> R.color.category_wallet
            else -> R.color.category_others
        }
    }

    private fun getBgColorRes(cat: String): Int {
        return when (cat) {
            "food", "dining" -> R.color.category_food_bg
            "shopping" -> R.color.category_shopping_bg
            "clothing" -> R.color.category_clothing_bg
            "transportation", "transport", "fuel", "gas" -> R.color.category_transport_bg
            "car" -> R.color.category_car_bg
            "bills", "utility", "rent" -> R.color.category_bills_bg
            "phone" -> R.color.category_phone_bg
            "home emi", "housing", "home" -> R.color.category_home_emi_bg
            "entertainment" -> R.color.category_entertainment_bg
            "health" -> R.color.category_health_bg
            "education" -> R.color.category_education_bg
            "travel", "trip" -> R.color.category_travel_bg
            "beauty" -> R.color.category_beauty_bg
            "sports" -> R.color.category_sports_bg
            "social" -> R.color.category_social_bg
            "alcohol" -> R.color.category_alcohol_bg
            "cigarettes" -> R.color.category_cigarettes_bg
            "electronics" -> R.color.category_electronics_bg
            "pets" -> R.color.category_pets_bg
            "repairs" -> R.color.category_repairs_bg
            "gifts" -> R.color.category_gifts_bg
            "donations" -> R.color.category_donations_bg
            "lottery" -> R.color.category_lottery_bg
            "snacks" -> R.color.category_snacks_bg
            "kids" -> R.color.category_kids_bg
            "vegetables" -> R.color.category_vegetables_bg
            "fruits" -> R.color.category_fruits_bg
            "old money" -> R.color.category_old_money_bg
            "salary" -> R.color.category_salary_bg
            "bonus" -> R.color.category_bonus_bg
            "business" -> R.color.category_business_bg
            "freelance", "part-time" -> R.color.category_freelance_bg
            "investment", "investments" -> R.color.category_investment_bg
            "bank transfer" -> R.color.category_bank_transfer_bg
            "wallet" -> R.color.category_wallet_bg
            else -> R.color.category_others_bg
        }
    }
}
