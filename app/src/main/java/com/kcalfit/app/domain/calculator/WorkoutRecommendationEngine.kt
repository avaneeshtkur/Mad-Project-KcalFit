package com.kcalfit.app.domain.calculator

data class WorkoutDay(
    val dayNumber: Int,
    val dayName: String,
    val focusArea: String,
    val exercises: List<String>,
    val durationMinutes: Int,
    val isRestDay: Boolean = false
)

data class WorkoutPlan(
    val planName: String,
    val daysPerWeek: Int,
    val goal: String,
    val days: List<WorkoutDay>
)

object WorkoutRecommendationEngine {

    fun generatePlan(
        goal: FitnessGoal,
        activityLevel: ActivityLevel,
        daysPerWeek: Int = 0
    ): WorkoutPlan {
        val effectiveDays = if (daysPerWeek in 2..7) daysPerWeek else when (activityLevel) {
            ActivityLevel.SEDENTARY -> 3
            ActivityLevel.LIGHTLY_ACTIVE -> 3
            ActivityLevel.MODERATELY_ACTIVE -> 4
            ActivityLevel.VERY_ACTIVE -> 5
            ActivityLevel.EXTRA_ACTIVE -> 6
        }

        return when (goal) {
            FitnessGoal.LOSE_WEIGHT -> generateFatLossPlan(effectiveDays)
            FitnessGoal.MAINTAIN -> generateMaintenancePlan(effectiveDays)
            FitnessGoal.GAIN_WEIGHT -> generateMuscleGainPlan(effectiveDays)
        }
    }

    private fun generateFatLossPlan(days: Int): WorkoutPlan {
        val allDays = listOf(
            WorkoutDay(1, "Monday", "Full Body HIIT", listOf("Burpees", "Jump Squats", "Mountain Climbers", "Push-ups", "Plank"), 30),
            WorkoutDay(2, "Tuesday", "Cardio + Core", listOf("Running/Cycling 20 min", "Crunches", "Leg Raises", "Russian Twists", "Plank Hold"), 35),
            WorkoutDay(3, "Wednesday", "Upper Body Circuit", listOf("Push-ups", "Dumbbell Rows", "Shoulder Press", "Tricep Dips", "Bicep Curls"), 30),
            WorkoutDay(4, "Thursday", "Rest & Recovery", emptyList(), 0, isRestDay = true),
            WorkoutDay(5, "Friday", "Lower Body + Cardio", listOf("Squats", "Lunges", "Glute Bridges", "Calf Raises", "Jump Rope 10 min"), 35),
            WorkoutDay(6, "Saturday", "Full Body Strength", listOf("Deadlifts", "Bench Press", "Pull-ups", "Overhead Press", "Plank"), 40),
            WorkoutDay(7, "Sunday", "Active Recovery", listOf("Light Walk 30 min", "Stretching", "Yoga Flow"), 30)
        )
        return WorkoutPlan("Fat Loss Plan", days, "Lose Weight", allDays.take(days))
    }

    private fun generateMaintenancePlan(days: Int): WorkoutPlan {
        val allDays = listOf(
            WorkoutDay(1, "Monday", "Upper Body", listOf("Bench Press", "Dumbbell Rows", "Shoulder Press", "Bicep Curls", "Tricep Extensions"), 40),
            WorkoutDay(2, "Tuesday", "Lower Body", listOf("Squats", "Romanian Deadlifts", "Leg Press", "Calf Raises", "Lunges"), 40),
            WorkoutDay(3, "Wednesday", "Cardio & Flexibility", listOf("30 min Jog/Cycle", "Dynamic Stretching", "Foam Rolling"), 35),
            WorkoutDay(4, "Thursday", "Push Day", listOf("Incline Press", "Lateral Raises", "Push-ups", "Overhead Tricep Extension", "Cable Flies"), 40),
            WorkoutDay(5, "Friday", "Pull Day", listOf("Deadlifts", "Pull-ups", "Barbell Rows", "Face Pulls", "Hammer Curls"), 40),
            WorkoutDay(6, "Saturday", "Full Body", listOf("Squats", "Bench Press", "Rows", "Shoulder Press", "Plank"), 45),
            WorkoutDay(7, "Sunday", "Rest", emptyList(), 0, isRestDay = true)
        )
        return WorkoutPlan("Maintenance Plan", days, "Maintain Weight", allDays.take(days))
    }

    private fun generateMuscleGainPlan(days: Int): WorkoutPlan {
        val allDays = listOf(
            WorkoutDay(1, "Monday", "Chest & Triceps", listOf("Bench Press 4x8", "Incline Dumbbell Press 3x10", "Cable Flies 3x12", "Tricep Pushdowns 3x12", "Overhead Tricep Extension 3x10"), 50),
            WorkoutDay(2, "Tuesday", "Back & Biceps", listOf("Deadlifts 4x6", "Barbell Rows 4x8", "Lat Pulldowns 3x10", "Barbell Curls 3x10", "Hammer Curls 3x12"), 50),
            WorkoutDay(3, "Wednesday", "Legs & Glutes", listOf("Squats 4x8", "Leg Press 3x10", "Romanian Deadlifts 3x10", "Leg Curls 3x12", "Calf Raises 4x15"), 50),
            WorkoutDay(4, "Thursday", "Rest & Recovery", emptyList(), 0, isRestDay = true),
            WorkoutDay(5, "Friday", "Shoulders & Arms", listOf("Overhead Press 4x8", "Lateral Raises 3x12", "Front Raises 3x12", "Barbell Curls 3x10", "Skull Crushers 3x10"), 45),
            WorkoutDay(6, "Saturday", "Full Body Power", listOf("Power Cleans 4x5", "Squats 3x8", "Bench Press 3x8", "Pull-ups 3x max", "Plank 3x60s"), 50),
            WorkoutDay(7, "Sunday", "Active Rest", listOf("Light Walk", "Stretching", "Mobility Work"), 25)
        )
        return WorkoutPlan("Muscle Gain Plan", days, "Gain Weight", allDays.take(days))
    }

    fun getPlanSummary(plan: WorkoutPlan): String {
        val workoutDays = plan.days.filter { !it.isRestDay }
        val restDays = plan.days.filter { it.isRestDay }
        val totalMinutes = workoutDays.sumOf { it.durationMinutes }
        return "${plan.planName}: ${workoutDays.size} workout days, ${restDays.size} rest days, ~${totalMinutes} min/week"
    }
}
