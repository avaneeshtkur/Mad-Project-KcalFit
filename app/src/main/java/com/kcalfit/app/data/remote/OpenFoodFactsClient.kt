package com.kcalfit.app.data.remote

import com.kcalfit.app.BuildConfig
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.search.AndroidFoodSearchDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.math.roundToInt

/** Read-only provider. Caching/ranking belong to FoodSearchRepository. */
class OpenFoodFactsClient(
    private val transport: FoodHttpTransport = UrlConnectionFoodTransport(
        "kcalfit/${BuildConfig.VERSION_NAME} (${BuildConfig.OPEN_FOOD_FACTS_CONTACT.ifBlank { BuildConfig.APPLICATION_ID }})"
    ),
    private val searchPolicy: FoodRequestPolicy = FoodRequestPolicy(diagnostics = AndroidFoodSearchDiagnostics),
    private val productPolicy: FoodRequestPolicy = FoodRequestPolicy(minIntervalMillis = 4_100, diagnostics = AndroidFoodSearchDiagnostics),
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val baseUrl = "https://world.openfoodfacts.org"
    private val fields = "code,product_name,brands,categories,serving_quantity,serving_quantity_unit,nutrition_data_per,nutriments"

    suspend fun search(query: String, pageSize: Int = 60): List<FoodItemEntity> {
        val url = "$baseUrl/cgi/search.pl?search_terms=${encode(query.trim())}&search_simple=1&action=process&json=1&page=1&page_size=${pageSize.coerceIn(1, 60)}&fields=$fields"
        val json = request(url, searchPolicy)
        return withContext(Dispatchers.Default) {
            val products = json.optJSONArray("products")
                ?: throw IllegalStateException("Food service returned an invalid search response. Please retry later.")
            buildList {
                for (index in 0 until products.length()) parseProduct(products.optJSONObject(index))?.let(::add)
            }.distinctBy { it.barcode }
        }
    }

    suspend fun lookupBarcode(barcode: String): FoodItemEntity? {
        val json = request("$baseUrl/api/v2/product/${encode(barcode.trim())}?fields=$fields", productPolicy)
        return if (json.optInt("status") != 1) null else parseProduct(json.optJSONObject("product"), barcode)
    }

    private suspend fun request(url: String, policy: FoodRequestPolicy): JSONObject {
        val response = policy.execute {
            transport.get(url).also {
                if (it.status !in 200..299) throw FoodServiceException(it.status, FoodRequestPolicy.retryAfterMillis(it.retryAfter, clock()))
            }
        }
        return withContext(Dispatchers.Default) {
            try { JSONObject(response.body) }
            catch (_: Exception) { throw IllegalStateException("Food service returned unreadable data. Please retry later.") }
        }
    }

    private fun parseProduct(product: JSONObject?, fallbackBarcode: String? = null): FoodItemEntity? {
        if (product == null) return null
        val name = product.optString("product_name").trim().ifBlank { return null }
        val barcode = product.optString("code").trim().ifBlank { fallbackBarcode ?: return null }
        val nutrients = product.optJSONObject("nutriments") ?: return null
        val per100 = product.optString("nutrition_data_per").equals("100g", true)
        val serving = number(product, "serving_quantity")?.takeIf { it > 0 }
        val servingInGrams = product.optString("serving_quantity_unit").equals("g", true)
        fun nutrient(key: String): Double? {
            number(nutrients, "${key}_100g")?.let { return it.takeIf { value -> value >= 0 } }
            val value = number(nutrients, key)?.takeIf { it >= 0 } ?: return null
            return when {
                per100 -> value
                servingInGrams && serving != null -> value * 100.0 / serving
                else -> null // Do not infer a mass from ml or an unspecified serving.
            }
        }
        val calories = nutrient("energy-kcal") ?: nutrient("energy")?.div(4.184) ?: return null
        if (calories > Int.MAX_VALUE) return null
        val protein = nutrient("proteins")
        val carbs = nutrient("carbohydrates")
        val fat = nutrient("fat")
        return FoodItemEntity(
            name = name, brand = product.optString("brands").trim().ifBlank { "Generic" },
            category = product.optString("categories").split(',').firstOrNull()?.trim().orEmpty().ifBlank { "General" },
            servingSize = 100.0, servingUnit = "g", calories = calories.roundToInt(),
            // Legacy non-null Room columns use zero placeholders. nutritionKnown=false
            // explicitly marks an incomplete macro set; zero is never claimed as measured.
            proteinGrams = protein ?: 0.0, carbsGrams = carbs ?: 0.0, fatGrams = fat ?: 0.0,
            fiberGrams = nutrient("fiber") ?: 0.0, sugarGrams = nutrient("sugars") ?: 0.0,
            sodiumMg = (nutrient("sodium") ?: 0.0) * 1000.0, barcode = barcode,
            nutritionKnown = protein != null && carbs != null && fat != null,
            source = "open_food_facts"
        )
    }

    private fun number(json: JSONObject, key: String): Double? =
        if (!json.has(key) || json.isNull(key)) null else json.optDouble(key, Double.NaN).takeIf { it.isFinite() }
    private fun encode(value: String) = URLEncoder.encode(value, Charsets.UTF_8.name())
}
