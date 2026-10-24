package com.kcalfit.app.domain.chatbot

import com.kcalfit.app.data.model.*
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.CancellationException

enum class CoachIntent { PROGRESS, MACROS, MEAL, FOOD, ALTERNATIVES, WORKOUT, TECHNIQUE, GOAL, NAVIGATE, DELETE_HELP, CLARIFY, OUTSIDE }
enum class CoachDestination { NUTRITION, ADD_FOOD, TRACK, PROGRESS, EXERCISES, HOME }
data class CoachAction(val destination: CoachDestination, val meal: MealType? = null, val date: String? = null, val food: FoodItemEntity? = null)
data class CoachRequest(val intent: CoachIntent, val date: String, val meal: MealType? = null,
    val food: FoodItemEntity? = null, val amount: Double? = null, val unit: String? = null,
    val action: CoachAction? = null, val subject: String = "")
data class CoachReply(val message: String, val action: CoachAction? = null)
data class CoachSnapshot(val entries: List<FoodEntry>, val foods: List<FoodItemEntity>, val goal: Int, val selectedDate: String, val exerciseDates: List<String> = emptyList(), val workoutDaysGoal: Int = 0, val proteinGoal: Int = 0, val carbsGoal: Int = 0, val fatGoal: Int = 0)
/** Optional server implementation must authenticate the user; never ship a provider secret in Android. */
fun interface CoachResponseService { suspend fun respond(question: String): String? }
class CoachResponder(private val remote: CoachResponseService? = null, private val timeoutMillis: Long = 8_000) {
    suspend fun respond(question: String, fallback: CoachReply, eligible: Boolean): CoachReply {
        if (!eligible || remote == null) return fallback
        return try {
            val text = withTimeoutOrNull(timeoutMillis) { remote.respond(question) }
            text?.takeIf { it.isNotBlank() }?.let { CoachReply(it.take(6000)) } ?: fallback
        } catch (e: CancellationException) { throw e } catch (_: Exception) { fallback }
    }
}

/** Local intent engine. No network and no writes; all numbers come from supplied Room records. */
class FitnessChatbot(private val clockMillis: () -> Long = System::currentTimeMillis) {
    private var previous: CoachRequest? = null
    private var lastTurn = 0L
    private var turns = 0
    fun reset() { previous = null; turns = 0; lastTurn = 0 }

    fun recognize(input: String, data: CoachSnapshot, today: LocalDate = LocalDate.now()): CoachRequest {
        val q = input.lowercase(Locale.ROOT).replace('’', '\'').trim()
        fun has(pattern: String) = Regex(pattern).containsMatchIn(q)
        val follow = has("^(what about|and |how much have i got left|[0-9]+|one |two |three )")
        val date = when {
            has("\\btoday\\b") -> today.toString()
            has("\\byesterday\\b") -> today.minusDays(1).toString()
            has("selected (date|day)") -> data.selectedDate
            else -> Regex("\\d{4}-\\d{2}-\\d{2}").find(q)?.value
                ?: if (follow) previous?.date ?: today.toString() else today.toString()
        }
        if (runCatching { LocalDate.parse(date) }.isFailure) return CoachRequest(CoachIntent.CLARIFY, today.toString(), subject = "Use a valid date such as 2026-10-14.")
        val meal = MealType.entries.firstOrNull { has("\\b${it.displayName.lowercase().removeSuffix("s")}s?\\b") }
        val normalized = q.replace("grams", "g").replace("gram", "g").replace("milliliters", "ml")
        val amountMatch = Regex("(?<![\\w.])([+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)|one|two|three|four)\\s*(g|kg|ml|servings?|eggs?)?\\b").find(normalized)
        val parsedAmount = amountMatch?.groupValues?.get(1)?.let { it.toDoubleOrNull() ?: mapOf("one" to 1.0,"two" to 2.0,"three" to 3.0,"four" to 4.0)[it] }
        val parsedUnit = amountMatch?.groupValues?.get(2)?.takeIf { it.isNotEmpty() }?.removeSuffix("s")
        val amount = parsedAmount ?: if (follow) previous?.amount else null
        val unit = parsedUnit ?: if (follow && parsedAmount == null) previous?.unit else null
        val exactFoods = data.foods.filter {
            val name = it.name.lowercase(Locale.ROOT).removeSuffix("s")
            Regex("\\b${Regex.escape(name)}s?\\b").containsMatchIn(q)
        }
        // Never map raw/fried rice to steamed rice merely because both contain "rice".
        val food = exactFoods.maxByOrNull { it.name.length }
            ?: if (follow && !has("\\b(raw|fried|brown|white|scrambled)\\b")) previous?.food else null
        fun request(i: CoachIntent, subject: String = "", action: CoachAction? = null) =
            CoachRequest(i,date,meal ?: if(follow) previous?.meal else null,food,amount,unit,action,subject)
        if (has("\\b(delete|remove)\\b.*\\b(food|entry|meal)\\b")) return request(CoachIntent.DELETE_HELP)
        val explicitOpen = has("^(please )?(open|show me|take me|go to|navigate|add|log|record)\\b")
        if (explicitOpen) {
            val dest = when {
                has("\\b(add|log|record)\\b.*\\b(food|meal|breakfast|lunch|dinner|snack)\\b") -> CoachDestination.ADD_FOOD
                food != null && has("^(please )?(add|log|record)\\b") -> CoachDestination.ADD_FOOD
                has("nutrition|food tracker|breakfast|lunch|dinner|snacks") -> CoachDestination.NUTRITION
                has("progress|goals") -> CoachDestination.PROGRESS
                has("exercise|workout") -> CoachDestination.EXERCISES
                has("track") -> CoachDestination.TRACK
                has("home") -> CoachDestination.HOME
                else -> null
            }
            if (dest != null && !has("\\b(don't|do not|not)\\b")) return request(CoachIntent.NAVIGATE, action = CoachAction(dest,meal,date, if(dest == CoachDestination.ADD_FOOD) food else null))
        }
        if (has("lower.*calorie|less.*calorie|lighter.*(food|option|meal)"))
            return CoachRequest(CoachIntent.ALTERNATIVES,date,food = previous?.food,amount = previous?.amount,unit = previous?.unit)
        if (has("\\b(protein|carbs?|carbohydrates|fats?|macros?)\\b") && food == null && has("my|i |eaten|consumed|today|yesterday|what about|left|remaining")) return request(CoachIntent.MACROS,q)
        if (follow && previous?.intent in setOf(CoachIntent.PROGRESS,CoachIntent.MACROS) &&
            has("^what about (today|yesterday|\\d{4}-\\d{2}-\\d{2})[?. ]*$")) return request(previous!!.intent,q)
        if (has("\\b(left|remaining|allowance|target|more can i eat)\\b") && has("calorie|eat|allowance|target|left")) return request(CoachIntent.PROGRESS)
        if (meal != null && has("eat|ate|record|log|had|review|what")) return request(CoachIntent.MEAL)
        if (has("what.*(eat|ate|eaten|had)|review.*meal|list.*food")) return request(CoachIntent.MEAL)
        if (has("(how (do|should|to)|technique|perform|correctly|form).*(squat|plank|push.?up)|squat.*correct")) return request(CoachIntent.TECHNIQUE, q)
        if (has("workout|exercise|training plan") && has("recommend|give|suggest|beginner|plan")) return request(CoachIntent.WORKOUT,q)
        if (has("\\b(calories?|protein|carbs?|fat)\\b") && has("\\b(in|of)\\b") && has("\\brice\\b") && !has("steamed|boiled|cooked|raw|fried"))
            return request(CoachIntent.CLARIFY,"What type of rice and preparation do you mean, and how much? For example, steamed rice with a quantity and unit.")
        if (food != null && (has("calorie|protein|carb|fat|how many|what about") || follow)) return request(CoachIntent.FOOD)
        if (has("\\b(protein|carbs?|carbohydrates|fats?|macros?)\\b") && has("my|i |eaten|consumed|today|yesterday|what about")) return request(CoachIntent.MACROS,q)
        if (has("calorie|intake") && has("i |my|today|consumed|eaten|progress")) return request(CoachIntent.PROGRESS)
        if (has("goal|weight management|lose weight|gain weight")) return request(CoachIntent.GOAL)
        if (has("where|how") && has("nutrition|track|app|food entr"))
            return request(CoachIntent.CLARIFY,"Use Nutrition for meal entries, Track for activity and progress, and the trash icon beside a saved food to delete it. Say ‘Open Nutrition’ to get a shortcut.")
        if (has("workout|exercise")) return request(CoachIntent.CLARIFY,"Would you like a beginner workout, technique for squats, planks or push-ups, or to open Exercises?")
        if (has("water|hydration|drink|dehydrat")) return request(CoachIntent.CLARIFY,"Hydration supports training and recovery. Sip water regularly through the day, drink more around sweaty exercise, and use thirst and pale urine as simple guides. If you have a fluid restriction or medical condition, follow your clinician's advice.")
        if (has("sleep|rest|recover|recovery|sore|muscle pain")) return request(CoachIntent.CLARIFY,"Recovery matters as much as training. Prioritize consistent sleep, easy movement on rest days, and gradual increases in workload. Stop exercise for sharp pain, chest pain, faintness or unusual shortness of breath.")
        if (has("warm.?up|stretch|cool.?down|mobility")) return request(CoachIntent.CLARIFY,"Warm up with five to ten minutes of easy movement, then use controlled range-of-motion drills for the muscles you will train. Cool down with easy movement and comfortable stretches; never force a painful range.")
        if (has("calorie|protein|nutrition|food|serving|rice|egg")) return request(CoachIntent.CLARIFY,"Which food and preparation, and how much (for example, grams or the package serving)? If this food isn't in the local catalogue, use the package label in Add Food; I don't have an online nutrition database.")
        return request(CoachIntent.OUTSIDE)
    }
    fun answer(input: String, data: CoachSnapshot, today: LocalDate = LocalDate.now()): Pair<CoachRequest, CoachReply> {
        val now = clockMillis()
        if (now - lastTurn > 15 * 60_000 || now < lastTurn || turns >= 12) reset()
        val r = recognize(input, data, today)
        lastTurn = now; turns++
        previous = if (r.intent in setOf(CoachIntent.OUTSIDE, CoachIntent.NAVIGATE)) null else r
        val entries = data.entries.filter { it.date == r.date }
        val totals = FoodDiaryRules.totals(if(r.intent == CoachIntent.MACROS && r.meal != null)
            FoodDiaryRules.entriesFor(entries,r.date,r.meal) else entries)
        fun n(v: Double) = String.format(Locale.getDefault(), "%.1f", v)
        val reply = when(r.intent) {
            CoachIntent.PROGRESS -> {
                val difference = data.goal - totals.calories
                CoachReply("On ${r.date}, you've recorded ${totals.calories} of ${data.goal} kcal. " +
                    if (difference >= 0) "$difference kcal remain against your saved goal." else "That's ${-difference} kcal above your saved goal. One day doesn't define your progress.")
            }
            CoachIntent.MACROS -> CoachReply("On ${r.date}: ${n(totals.protein)} g protein, ${n(totals.carbs)} g carbohydrates, ${n(totals.fat)} g fat." +
                if (totals.missingMacros > 0) " These are partial totals: ${totals.missingMacros} entries lack complete macros." else if(entries.isEmpty()) " No foods are recorded yet." else " From your saved food entries." + if(r.subject.contains("left") || r.subject.contains("remaining"))
                    " Remaining against your saved macro goals: ${n((data.proteinGoal - totals.protein).coerceAtLeast(0.0))} g protein, ${n((data.carbsGoal - totals.carbs).coerceAtLeast(0.0))} g carbs, ${n((data.fatGoal - totals.fat).coerceAtLeast(0.0))} g fat." else "")
            CoachIntent.MEAL -> {
                val foods = if(r.meal == null) entries else FoodDiaryRules.entriesFor(entries,r.date,r.meal)
                CoachReply("${r.meal?.displayName ?: "Food diary"} · ${r.date}\n" + if(foods.isEmpty()) "No food recorded. Open Nutrition to add an entry." else foods.joinToString("\n") { "• ${it.foodName}, ${it.quantity}: ${it.calories} kcal" })
            }
            CoachIntent.FOOD -> {
                val f = r.food!!
                val baseUnit = f.servingUnit.lowercase().removeSuffix("s")
                val suppliedUnit = if(r.unit == "kg") "g" else r.unit
                val compatible = suppliedUnit != null && (suppliedUnit == baseUnit || suppliedUnit == "egg" && baseUnit == "piece")
                if (r.amount == null || !compatible) CoachReply("${f.name}: the catalogue uses ${f.servingSize} ${f.servingUnit}. How many ${f.servingUnit}? I can't convert a count or serving to grams without a known weight.")
                else {
                    val factor = runCatching { FoodDiaryRules.multiplier(r.amount * if(r.unit == "kg") 1000 else 1,f.servingSize) }.getOrNull()
                    if (factor == null) CoachReply("Please enter a positive, reasonable quantity.")
                    else CoachReply("Catalogue estimate for ${r.amount} ${r.unit} of ${f.name}: ${FoodDiaryRules.calories(f.calories,factor)} kcal." +
                        if(f.nutritionKnown) " Protein ${n(f.proteinGrams*factor)} g; carbohydrates ${n(f.carbsGrams*factor)} g; fat ${n(f.fatGrams*factor)} g. Preparation and brands vary; check the label." else " Complete macros aren't available. Check the package or enter verified nutrition manually.")
                }
            }
            CoachIntent.ALTERNATIVES -> {
                val original = r.food
                if(original == null) CoachReply("Which food would you like to compare? Include a quantity and preparation so I can compare like portions.")
                else {
                    val alternatives = data.foods.filter { it.name != original.name && it.servingUnit == original.servingUnit && it.servingSize > 0 &&
                        it.calories / it.servingSize < original.calories / original.servingSize }.sortedBy { it.calories / it.servingSize }.take(3)
                    CoachReply(if(alternatives.isEmpty()) "I don't have a comparable lower-calorie option with the same unit in the local catalogue. Check portions and package labels; I won't guess a conversion."
                        else "Catalogue estimates per ${original.servingSize} ${original.servingUnit}, for comparison only:\n" + alternatives.joinToString("\n") {
                            "• ${it.name}: ${FoodDiaryRules.calories(it.calories,FoodDiaryRules.multiplier(original.servingSize,it.servingSize))} kcal"
                        } + "\nThese aren't equivalent meals; ingredients and nutrition differ. Check the label and your dietary needs.")
                }
            }
            CoachIntent.NAVIGATE -> CoachReply(if(r.action!!.destination == CoachDestination.ADD_FOOD) "Open Add Food to review quantity and nutrition before saving. Nothing has been logged." else "Open your ${r.action.destination.name.lowercase().replace('_',' ')} screen below.",r.action)
            CoachIntent.DELETE_HELP -> CoachReply("Open Nutrition, select the date and meal, then tap the trash icon beside the individual entry and confirm. Matching food names remain separate entries.",CoachAction(CoachDestination.NUTRITION,r.meal,r.date))
            CoachIntent.WORKOUT -> CoachReply(if(r.subject.contains("leg")) "Beginner legs\n• Warm up with an easy walk.\n• Practice controlled chair squats.\n• Add supported reverse lunges and calf raises.\nStart with a short comfortable set of each, rest between movements, and increase gradually. Stop for pain. Open Exercises for the app's guided sessions." else "Beginner session\n• Warm up with easy movement.\n• Alternate chair squats, wall push-ups and a comfortable walk.\n• Finish with gentle mobility.\nKeep the first session short and controlled; rest as needed. Open Exercises to choose a guided session.")
            CoachIntent.TECHNIQUE -> CoachReply(when {
                r.subject.contains("plank") -> "Plank: place elbows beneath shoulders, brace gently, and keep head, hips and heels aligned. Breathe normally; lower your knees to make it easier. Stop if your back hurts."
                r.subject.contains("push") -> "Push-up: start against a wall if needed. Keep hands around shoulder width and your trunk aligned. Bend elbows under control, then press away while breathing. Use a smaller range if needed; stop for pain."
                else -> "Squat: set feet comfortably apart, brace your trunk, and sit down and back with control. Let knees follow your toes and keep your feet grounded. Stand by pressing through your feet. Use a chair for support and a comfortable range; stop for pain."
            })
            CoachIntent.GOAL -> {
                val week = FoodDiaryRules.week(LocalDate.parse(r.date)).map { it.toString() }
                val active = data.exerciseDates.filter { it in week }.distinct().size
                val activity = if(data.workoutDaysGoal > 0) " You've logged activity on $active of ${data.workoutDaysGoal} target days this week." else " No weekly activity target is available."
                CoachReply("Your saved calorie target is ${data.goal} kcal; ${totals.calories} kcal are recorded for ${r.date}. $activity Review your diary for missing entries, then use Track to review your activity. Device steps aren't connected, so I can't judge your step goal. For weight goals, use the goal settings rather than skipping meals to compensate.",CoachAction(CoachDestination.TRACK))
            }
            CoachIntent.CLARIFY -> CoachReply(r.subject)
            CoachIntent.OUTSIDE -> CoachReply("I focus on kcalfit, food, fitness and your recorded progress. Try ‘What did I eat for breakfast today?’ or ‘Open Nutrition’. For medical symptoms or treatment, contact a qualified clinician.")
        }
        return r to reply
    }
}
