package com.kcalfit.app.data.importer

import android.content.Context
import com.kcalfit.app.data.local.FoodItemDao
import com.kcalfit.app.data.model.FoodItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object FoodAssetImporter {
    private const val FILE_NAME = "foods.csv"

    suspend fun importIfNeeded(context: Context, dao: FoodItemDao) = withContext(Dispatchers.IO) {
        // Query Room: a SharedPreferences flag survives database migrations and can
        // incorrectly suppress importing after the table has been cleared.
        if (dao.getImportedFoodCount() > 0) return@withContext

        try {
            context.assets.open(FILE_NAME).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    val header = reader.readLine()?.let(::parseCsvLine) ?: return@withContext
                    val columns = header.mapIndexed { index, value -> value.trim().lowercase() to index }.toMap()
                    fun indexOf(vararg names: String): Int = names.firstNotNullOfOrNull { columns[it] } ?: -1

                    val nameIndex = indexOf("food_name")
                    val caloriesIndex = indexOf("calories")
                    val proteinIndex = indexOf("protein")
                    val carbsIndex = indexOf("carbs")
                    val fatIndex = indexOf("fat")
                    val fiberIndex = indexOf("fiber", "fiber_g")
                    val sugarIndex = indexOf("sugar", "sugar_g")
                    val sodiumIndex = indexOf("sodium_mg")
                    val servingSizeIndex = indexOf("serving_size")
                    val servingUnitIndex = indexOf("serving_unit")
                    if (nameIndex < 0) return@withContext

                    val foods = buildList {
                        reader.lineSequence().take(5_000).forEach { line ->
                            val values = parseCsvLine(line)
                            val name = values.getOrNull(nameIndex).orEmpty().trim()
                            val calories = values.getOrNull(caloriesIndex)?.toDoubleOrNull()
                                ?.takeIf { it.isFinite() && it >= 0 && it <= Int.MAX_VALUE } ?: return@forEach
                            if (name.isNotEmpty()) add(
                                FoodItemEntity(
                                    name = name,
                                    brand = "Imported dataset",
                                    category = "General",
                                    servingSize = values.getOrNull(servingSizeIndex)?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 } ?: 1.0,
                                    servingUnit = values.getOrNull(servingUnitIndex)?.takeIf { it.isNotBlank() } ?: "serving",
                                    calories = calories.toInt(),
                                    proteinGrams = values.decimalAt(proteinIndex),
                                    carbsGrams = values.decimalAt(carbsIndex),
                                    fatGrams = values.decimalAt(fatIndex),
                                    fiberGrams = values.decimalAt(fiberIndex),
                                    sugarGrams = values.decimalAt(sugarIndex),
                                    sodiumMg = values.decimalAt(sodiumIndex),
                                    nutritionKnown = listOf(proteinIndex, carbsIndex, fatIndex).all { index ->
                                        values.getOrNull(index)?.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true
                                    }
                                )
                            )
                        }
                    }
                    if (foods.isNotEmpty()) dao.insertFoodItems(foods)
                }
            }
        } catch (_: java.io.IOException) {
            // The app can still use the built-in fallback food seed if the asset is absent.
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var index = 0
        while (index < line.length) {
            val character = line[index]
            when {
                character == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"'); index++
                }
                character == '"' -> quoted = !quoted
                character == ',' && !quoted -> { values += current.toString(); current.clear() }
                else -> current.append(character)
            }
            index++
        }
        values += current.toString()
        return values
    }

    private fun List<String>.decimalAt(index: Int): Double =
        getOrNull(index)?.replace(",", "")?.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: 0.0
}
