package com.example.mind_mitra.data

import androidx.annotation.StringRes
import com.example.mind_mitra.R

object MemoryCategories {
    const val FAMILY = "family"
    const val CHILDHOOD = "childhood"
    const val SPECIAL_MOMENTS = "special_moments"
    const val PEOPLE = "people"
    const val PLACES = "places"
    const val ACHIEVEMENTS = "achievements"
    const val OTHER = "other"

    val ALL = listOf(
        FAMILY,
        CHILDHOOD,
        SPECIAL_MOMENTS,
        PEOPLE,
        PLACES,
        ACHIEVEMENTS,
        OTHER
    )

    @StringRes
    fun labelRes(category: String): Int = when (category) {
        FAMILY -> R.string.cat_family
        CHILDHOOD -> R.string.cat_childhood
        SPECIAL_MOMENTS -> R.string.cat_special_moments
        PEOPLE -> R.string.cat_people
        PLACES -> R.string.cat_places
        ACHIEVEMENTS -> R.string.cat_achievements
        else -> R.string.cat_other
    }

    fun normalize(raw: String?): String {
        val value = raw?.trim()?.lowercase()?.replace(" ", "_") ?: return OTHER
        return if (ALL.contains(value)) value else OTHER
    }
}
