package com.kcalfit.app.data.search

import com.kcalfit.app.data.model.FoodItemEntity
import java.text.Normalizer
import java.util.Locale

object FoodSearchText {
    private val marks = Regex("\\p{M}+")
    private val punctuation = Regex("[^\\p{L}\\p{N}]+")
    private val aliases = mapOf("chapati" to "roti", "chapathi" to "roti", "chappati" to "roti", "panir" to "paneer")
    fun normalize(value: String): String = punctuation.replace(
        marks.replace(Normalizer.normalize(value, Normalizer.Form.NFKD), "").lowercase(Locale.ROOT), " "
    ).trim()
    fun canonical(value: String): String = normalize(value).split(' ').joinToString(" ") { aliases[it] ?: it }
}

/** Pre-normalizes the bounded Room catalog once; ranking never changes food data. */
class FoodSearchIndex(foods: List<FoodItemEntity>) {
    private data class Candidate(val food: FoodItemEntity, val name: String, val canonical: String, val brand: String, val words: List<String>)
    private val candidates = foods.map {
        val canonical = FoodSearchText.canonical(it.name)
        Candidate(it, FoodSearchText.normalize(it.name), canonical, FoodSearchText.normalize(it.brand), canonical.split(' '))
    }

    fun search(query: String, limit: Int = 50): List<FoodItemEntity> {
        val normalized = FoodSearchText.normalize(query)
        if (normalized.isEmpty()) return emptyList()
        val canonical = FoodSearchText.canonical(query)
        val tokens = canonical.split(' ')
        return candidates.asSequence().mapNotNull { candidate ->
            val name = candidate.canonical
            val words = candidate.words
            val tier = when {
                candidate.name == normalized -> 0
                candidate.name.startsWith(normalized) -> 1
                name.startsWith(canonical) || name.contains(" $canonical") -> 2
                tokens.all { token -> words.any { it.startsWith(token) } } -> 3
                name.contains(canonical) || tokens.all(name::contains) -> 4
                tokens.all { token -> token.length >= 4 && words.any { editDistanceAtMostOne(token, it) } } -> 5
                candidate.brand.contains(normalized) -> 6
                else -> return@mapNotNull null
            }
            Triple(candidate, tier, kotlin.math.abs(name.length - canonical.length))
        }.sortedWith(compareBy<Triple<Candidate, Int, Int>> { it.second }
            .thenBy { it.third }.thenBy { it.first.name }.thenBy { it.first.brand }
            .thenBy { it.first.food.barcode.orEmpty() }.thenBy { it.first.food.id })
            .distinctBy { identity(it.first.food) }.take(limit).map { it.first.food }.toList()
    }

    companion object {
        fun identity(food: FoodItemEntity): String = food.barcode?.takeIf { it.isNotBlank() && !food.isCustom }?.let { "barcode:$it" }
            ?: listOf(FoodSearchText.normalize(food.name), FoodSearchText.normalize(food.brand), food.servingSize,
                food.servingUnit, food.calories, food.proteinGrams, food.carbsGrams, food.fatGrams).joinToString("|")

        private fun editDistanceAtMostOne(a: String, b: String): Boolean {
            if (kotlin.math.abs(a.length - b.length) > 1) return false
            var i = 0; var j = 0; var edits = 0
            while (i < a.length && j < b.length) {
                if (a[i] == b[j]) { i++; j++ } else {
                    if (++edits > 1) return false
                    if (a.length >= b.length) i++
                    if (b.length >= a.length) j++
                }
            }
            return edits + (a.length - i) + (b.length - j) <= 1
        }
    }
}
