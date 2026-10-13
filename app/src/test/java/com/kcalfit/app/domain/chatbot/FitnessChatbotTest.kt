package com.kcalfit.app.domain.chatbot

import com.kcalfit.app.data.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking

class FitnessChatbotTest {
    private val today = LocalDate.of(2026,10,14)
    private val rice = FoodItemEntity(name="Steamed Rice",servingSize=100.0,servingUnit="g",calories=130,proteinGrams=2.0,carbsGrams=28.0,fatGrams=0.3)
    private val entries = listOf(
        FoodEntry(id=1,foodName="Rice",mealType="Breakfast",calories=300,proteinGrams=6.0,carbsGrams=60.0,fatGrams=2.0,quantity="250 g",date=today.toString()),
        FoodEntry(id=2,foodName="Toast",mealType="Lunch",calories=200,quantity="2 slices",date=today.toString(),nutritionKnown=false),
        FoodEntry(id=3,foodName="Soup",mealType="Dinner",calories=150,quantity="1 bowl",date=today.minusDays(1).toString()))
    private val data = CoachSnapshot(entries,listOf(rice),2000,"2026-10-01")
    private fun reply(q:String) = FitnessChatbot().answer(q,data,today)
    @Test fun remainingPhrasingsUseTodaysRecordsNotSelectedHistory() {
        listOf("Calories left?","How much more can I eat today?","What's my remaining calorie allowance?","Have I reached my calorie target?").forEach {
            val (intent,answer)=reply(it); assertEquals(CoachIntent.PROGRESS,intent.intent); assertTrue(answer.message.contains("1500"))
        }
    }
    @Test fun mealReviewUsesDateAndMeal() {
        val r=reply("What did I eat for breakfast today?").second.message
        assertTrue(r.contains("Rice"));assertFalse(r.contains("Toast"));assertFalse(r.contains("Soup"))
        assertTrue(reply("What did I eat for dinner yesterday?").second.message.contains("Soup"))
    }
    @Test fun allMealsRecognized() {
        listOf("breakfast","lunch","dinner","snacks").forEach { assertEquals(CoachIntent.MEAL,reply("What did I eat for $it today?").first.intent) }
    }
    @Test fun macroTotalsDiscloseIncompleteRecords() {
        val r=reply("How much protein have I eaten?").second.message
        assertTrue(r.contains("6.0")); assertTrue(r.contains("partial"))
    }
    @Test fun quantityAndMacroFollowUpsRetainFood() {
        val coach=FitnessChatbot()
        assertTrue(coach.answer("Calories in steamed rice?",data,today).second.message.contains("How many"))
        assertTrue(coach.answer("250 grams",data,today).second.message.contains("325 kcal"))
        assertTrue(coach.answer("What about protein?",data,today).second.message.contains("5.0 g"))
    }
    @Test fun unknownServingWeightNeverConverted() {
        val d=data.copy(foods=listOf(rice.copy(servingSize=1.0,servingUnit="serving")))
        assertTrue(FitnessChatbot().answer("Calories in 250 g steamed rice",d,today).second.message.contains("can't convert"))
    }
    @Test fun ambiguousRiceClarifiesPreparation() { assertEquals(CoachIntent.CLARIFY,reply("How many calories in rice?").first.intent) }
    @Test fun newTopicClearsFoodContext() {
        val c=FitnessChatbot();c.answer("Calories in 250 g steamed rice",data,today)
        assertEquals(CoachIntent.OUTSIDE,c.answer("Write a poem about space",data,today).first.intent)
        assertEquals(CoachIntent.OUTSIDE,c.answer("250 grams",data,today).first.intent)
    }
    @Test fun actionsAreAllowlistedAndNeverWriteFood() {
        assertEquals(CoachDestination.NUTRITION,reply("Open my nutrition tracker").second.action?.destination)
        assertEquals(CoachDestination.ADD_FOOD,reply("Add food to lunch").second.action?.destination)
        assertEquals(MealType.LUNCH,reply("Add food to lunch").second.action?.meal)
        assertNull(reply("Don't open nutrition").second.action)
        assertTrue(reply("Where can I delete a food entry?").second.message.contains("confirm"))
    }
    @Test fun workoutAndTechniqueDistinct() {
        assertEquals(CoachIntent.WORKOUT,reply("Give me a beginner leg workout").first.intent)
        assertEquals(CoachIntent.TECHNIQUE,reply("How do I perform a squat correctly?").first.intent)
    }
    @Test fun invalidDateClarifies() { assertEquals(CoachIntent.CLARIFY,reply("Breakfast on 2026-99-99").first.intent) }
    @Test fun modelFailureFallsBackAndPersonalDataNeverSent() = runBlocking {
        var called=false
        val service=CoachResponder(CoachResponseService { called=true; error("offline") })
        val fallback=CoachReply("Local answer")
        assertEquals(fallback,service.respond("My calories",fallback,false));assertFalse(called)
        assertEquals(fallback,service.respond("Workout",fallback,true));assertTrue(called)
    }
    @Test fun rawRiceNeverUsesSteamedEstimate() {
        assertEquals(CoachIntent.CLARIFY,reply("Calories in 250 g raw rice").first.intent)
    }
    @Test fun contextExpiresAfterInactivity() {
        var clock=1000L
        val c=FitnessChatbot { clock }
        c.answer("Calories in steamed rice?",data,today)
        clock += 16*60_000
        assertEquals(CoachIntent.OUTSIDE,c.answer("250 grams",data,today).first.intent)
    }
    @Test fun explicitFoodAddOffersReviewWithCorrectCatalogueFood() {
        val a=reply("Add steamed rice to dinner").second.action
        assertEquals(CoachDestination.ADD_FOOD,a?.destination)
        assertEquals(MealType.DINNER,a?.meal)
        assertEquals(rice,a?.food)
    }
    @Test fun workoutProgressUsesDistinctDaysInWeek() {
        val d=data.copy(workoutDaysGoal=3,exerciseDates=listOf("2026-10-14","2026-10-14","2026-10-13","2026-09-01"))
        assertTrue(FitnessChatbot().answer("Help me reach my activity goal",d,today).second.message.contains("2 of 3"))
    }
    @Test fun macroQuestionDoesNotBecomeCalorieProgress() {
        assertEquals(CoachIntent.MACROS,reply("How much protein do I have left?").first.intent)
    }
    @Test fun dateFollowUpUsesNewDay() {
        val c=FitnessChatbot();c.answer("My calorie intake today",data,today)
        val r=c.answer("What about yesterday?",data,today)
        assertEquals(CoachIntent.PROGRESS,r.first.intent)
        assertTrue(r.second.message.contains("150 of 2000"))
    }
    @Test fun modelTimeoutUsesFallback() = runBlocking {
        val fallback=CoachReply("Local answer")
        val responder=CoachResponder(CoachResponseService { kotlinx.coroutines.delay(100); "Too late" },1)
        assertEquals(fallback,responder.respond("Workout",fallback,true))
    }
    @Test fun negativeQuantityIsRejectedNotMadePositive() {
        assertTrue(reply("Calories in -250 g steamed rice").second.message.contains("positive"))
    }
    @Test fun wholeDayReviewIncludesAllMealsForOnlyThatDate() {
        val r=reply("What did I eat today?").second.message
        assertTrue(r.contains("Rice"));assertTrue(r.contains("Toast"));assertFalse(r.contains("Soup"))
    }
}
