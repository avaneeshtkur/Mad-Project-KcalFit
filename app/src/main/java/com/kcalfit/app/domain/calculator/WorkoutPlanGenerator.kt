package com.kcalfit.app.domain.calculator

data class PlannedExercise(val name: String, val sets: Int, val reps: String, val restSeconds: Int, val category: String, val suggestedWeight: String = "Choose a challenging weight", val instruction: String = "Use controlled form and stop 1–2 reps before failure")
data class WorkoutDayPlan(val dayNumber: Int, val dayLabel: String, val focusArea: String, val exercises: List<PlannedExercise>)

object WorkoutPlanGenerator {
    private val chest = listOf("Bench Press", "Push-ups", "Dumbbell Flyes", "Incline Press", "Cable Crossover")
    private val back = listOf("Pull-ups", "Barbell Rows", "Lat Pulldown", "Deadlift", "Seated Cable Row")
    private val legs = listOf("Squats", "Lunges", "Leg Press", "Romanian Deadlift", "Calf Raises", "Leg Curls")
    private val core = listOf("Planks", "Crunches", "Russian Twists", "Leg Raises", "Mountain Climbers")
    private val cardio = listOf("Running", "Cycling", "Jump Rope", "Burpees", "Jumping Jacks", "HIIT intervals")
    private fun strength(names: List<String>, goal: FitnessGoal) = names.take(5).map { name -> PlannedExercise(name, if (goal == FitnessGoal.GAIN_WEIGHT) 4 else 3, if (name == "Planks") "30–45 sec" else "8–12", if (goal == FitnessGoal.GAIN_WEIGHT) 90 else 60, "STRENGTH", if (name in listOf("Push-ups", "Pull-ups", "Planks")) "Bodyweight" else "Start light; add weight when all reps are clean", "Warm up first; keep tempo controlled and full range of motion") }
    private fun cardio(goal: FitnessGoal) = cardio.take(if (goal == FitnessGoal.LOSE_WEIGHT) 2 else 1).map { PlannedExercise(it, 1, "20–30 min", 30, "CARDIO", "Moderate pace", "Keep a pace where you can speak in short sentences") }
    fun generatePlan(goal: FitnessGoal, exerciseDaysPerWeek: Int, activityLevel: ActivityLevel): List<WorkoutDayPlan> {
        val days = exerciseDaysPerWeek.coerceIn(1, 7)
        return (1..days).map { n ->
            val (label, focus, exercises) = when {
                days <= 2 -> Triple("Day $n - Full Body", "Full Body", strength(chest + back + legs, goal) + cardio(goal))
                days == 3 -> when (n) { 1 -> Triple("Day 1 - Push", "Upper Body", strength(chest, goal) + strength(listOf("Overhead Press", "Lateral Raises"), goal)); 2 -> Triple("Day 2 - Pull", "Back", strength(back, goal)); else -> Triple("Day 3 - Legs", "Lower Body", strength(legs, goal)) }
                days == 4 -> Triple("Day $n - ${if (n % 2 == 1) "Upper" else "Lower"}", if (n % 2 == 1) "Upper Body" else "Lower Body", strength(if (n % 2 == 1) chest + back else legs + core, goal))
                else -> Triple("Day $n - ${if (n % 3 == 1) "Push" else if (n % 3 == 2) "Pull" else "Legs"}", "Strength", strength(if (n % 3 == 1) chest else if (n % 3 == 2) back else legs, goal) + if (n == days || goal == FitnessGoal.LOSE_WEIGHT) cardio(goal) else emptyList())
            }
            WorkoutDayPlan(n, label, focus, exercises.take(6))
        }
    }
}
