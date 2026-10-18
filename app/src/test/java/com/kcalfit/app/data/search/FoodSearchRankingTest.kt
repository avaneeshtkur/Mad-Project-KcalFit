package com.kcalfit.app.data.search

import com.kcalfit.app.data.model.FoodItemEntity
import org.junit.Assert.*
import org.junit.Test

class FoodSearchRankingTest {
    private fun food(name: String, code: String? = name) = FoodItemEntity(name = name, barcode = code, calories = 100)

    @Test fun riceAndPrefixesOutrankUnrelatedSubstrings() {
        val index = FoodSearchIndex(listOf(food("Turmeric"), food("Paprica"), food("Rice cooked"), food("Brown rice"), food("Rice"), food("Rice raw")))
        assertEquals(listOf("Rice", "Rice raw", "Rice cooked"), index.search("RIC").take(3).map { it.name })
        assertEquals("Rice", index.search("RICE").first().name)
        assertTrue(index.search("RIC").indexOfFirst { it.name == "Brown rice" } < index.search("RIC").indexOfFirst { it.name == "Turmeric" })
    }

    @Test fun exactPrefixBoundaryTokensSubstringAndFuzzyAreDeterministic() {
        val foods = listOf(food("Chicken curry"), food("Curry chicken"), food("Chicken"), food("Superchicken"), food("Chickpea"))
        assertEquals(listOf("Chicken", "Chicken curry", "Curry chicken", "Superchicken"), FoodSearchIndex(foods).search("chicken").map { it.name })
        assertEquals("Chicken", FoodSearchIndex(foods).search("chiken").first().name)
        assertEquals(listOf("Chicken", "Chickpea", "Chicken curry"), FoodSearchIndex(foods).search("CHI").take(3).map { it.name })
        assertEquals(FoodSearchIndex(foods).search("CHI"), FoodSearchIndex(foods.reversed()).search("CHI"))
    }

    @Test fun unicodeWhitespacePunctuationAliasesAndMultiwordQueriesNormalize() {
        assertEquals("creme brulee rice", FoodSearchText.normalize("  CRÈME—Brûlée  ＲＩＣＥ!! "))
        assertEquals("roti", FoodSearchText.canonical("Chapathi"))
        val foods = listOf(food("Rice brown cooked"), food("Brown rice"), food("Paneer"), food("Roti"), food("Chapati"))
        assertEquals("Brown rice", FoodSearchIndex(foods).search("brown rice").first().name)
        assertEquals("Roti", FoodSearchIndex(foods).search("ROT").first().name)
        assertEquals("Paneer", FoodSearchIndex(foods).search("PAN").first().name)
        assertEquals("Chapati", FoodSearchIndex(foods).search("chapati").first().name)
    }

    @Test fun deduplicationPreservesPreparationsAndNutritionalValues() {
        val raw = food("Rice raw", "1").copy(calories = 365)
        val cooked = food("Rice cooked", "2").copy(calories = 130)
        val results = FoodSearchIndex(listOf(raw, raw.copy(id = 9), cooked)).search("rice")
        assertEquals(2, results.size)
        assertEquals(setOf(130,365), results.map { it.calories }.toSet())
    }

    @Test fun measureBoundedLocalIndexWithoutNetwork() {
        val foods = (1..5000).map { food("Rice variety $it", "$it") }
        val buildStart = System.nanoTime()
        val index = FoodSearchIndex(foods)
        val buildMs = (System.nanoTime() - buildStart) / 1_000_000.0
        repeat(10) { index.search("RIC") }
        val times = (1..30).map { val start = System.nanoTime(); assertEquals(50, index.search("RIC").size); (System.nanoTime()-start)/1_000_000.0 }.sorted()
        println("FOOD_BENCH candidates=5000 buildMs=$buildMs medianMs=${times[15]} p95Ms=${times[28]}")
    }
}
