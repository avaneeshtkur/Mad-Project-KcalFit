package com.kcalfit.app.data.remote

import com.kcalfit.app.data.model.FoodItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

/** Read-only Open Food Facts client. It deliberately sends no credentials. */
class OpenFoodFactsClient {
    private val baseUrl = "https://world.openfoodfacts.org"
    private val userAgent = "KcalFit/1.0 (nutrition search; contact project maintainer)"

    suspend fun search(query: String, pageSize: Int = 20): List<FoodItemEntity> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val url = URL("$baseUrl/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=$pageSize&fields=code,product_name,brands,categories,serving_size,serving_quantity,serving_quantity_unit,nutrition_data_per,nutriments")
        request(url).let { json ->
            val products = json.optJSONArray("products") ?: return@let emptyList()
            buildList {
                for (index in 0 until products.length()) {
                    parseProduct(products.optJSONObject(index))?.let(::add)
                }
            }
        }
    }

    suspend fun lookupBarcode(barcode: String): FoodItemEntity? = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/api/v2/product/${URLEncoder.encode(barcode.trim(), Charsets.UTF_8.name())}?fields=code,product_name,brands,categories,serving_size,serving_quantity,serving_quantity_unit,nutrition_data_per,nutriments")
        val json = request(url)
        if (json.optInt("status") != 1) null else parseProduct(json.optJSONObject("product"), barcode)
    }

    private fun request(url: URL): JSONObject {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) throw IllegalStateException("Food service returned ${connection.responseCode}")
            return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProduct(product: JSONObject?, fallbackBarcode: String? = null): FoodItemEntity? {
        if (product == null) return null
        val name = product.optString("product_name").trim().ifBlank { return null }
        val nutrients = product.optJSONObject("nutriments") ?: return null
        val calories = number(nutrients, "energy-kcal_100g")
            ?: number(nutrients, "energy-kcal")
            ?: number(nutrients, "energy_100g")?.div(4.184)
            ?: number(nutrients, "energy")?.div(4.184)
        if (calories == null || calories < 0) return null
        val brand = product.optString("brands").trim().ifBlank { "Generic" }
        val servingSize = number(product, "serving_quantity")?.takeIf { it > 0 } ?: 100.0
        val servingUnit = product.optString("serving_quantity_unit").trim().ifBlank { "g" }
        val nutritionPer100g = product.optString("nutrition_data_per").equals("100g", true) ||
            nutrients.has("proteins_100g") || nutrients.has("carbohydrates_100g") || nutrients.has("fat_100g")
        val scale = if (nutritionPer100g || servingSize == 100.0) 1.0 else 100.0 / servingSize
        fun nutrient(key: String): Double {
            val value = number(nutrients, "${key}_100g") ?: number(nutrients, key) ?: return 0.0
            return (value * scale).coerceAtLeast(0.0)
        }
        return FoodItemEntity(
            name = name,
            brand = brand,
            category = product.optString("categories").split(',').firstOrNull()?.trim().orEmpty().ifBlank { "General" },
            servingSize = 100.0,
            servingUnit = "g",
            calories = calories.roundToInt(),
            proteinGrams = nutrient("proteins"),
            carbsGrams = nutrient("carbohydrates"),
            fatGrams = nutrient("fat"),
            fiberGrams = nutrient("fiber"),
            sugarGrams = nutrient("sugars"),
            sodiumMg = nutrient("sodium") * 1000.0,
            barcode = product.optString("code").ifBlank { fallbackBarcode },
            isCustom = false,
            nutritionKnown = nutrients.has("proteins_100g") || nutrients.has("proteins")
        )
    }

    private fun number(json: JSONObject, key: String): Double? =
        if (!json.has(key) || json.isNull(key)) null else json.optDouble(key, Double.NaN).takeIf { it.isFinite() }
}
