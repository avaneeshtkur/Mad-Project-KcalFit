package com.kcalfit.app.domain.workout

import com.kcalfit.app.domain.calculator.FitnessGoal
import java.time.DayOfWeek
import java.time.LocalDate

enum class WorkoutPhase(val label: String) { WARM_UP("Warm-up"), MAIN("Main workout"), FINISHER("Finisher"), COOL_DOWN("Cool-down") }

/** One exercise as prescribed for today. Reps-based moves have [reps]; timed moves have [seconds]. */
data class PlannedMove(
    val exercise: LibraryExercise,
    val phase: WorkoutPhase,
    val sets: Int,
    val reps: IntRange? = null,
    val seconds: Int? = null,
    val restSeconds: Int = 0
) {
    private val side get() = if (exercise.perSide) " each side" else ""

    val prescription: String get() {
        val work = when {
            reps != null -> "${if (reps.first == reps.last) "${reps.first}" else "${reps.first}–${reps.last}"} reps$side"
            else -> formatSeconds(seconds ?: 0) + side
        }
        return if (sets > 1) "$sets × $work" else work
    }

    val restText: String? get() = if (sets > 1 && restSeconds > 0) "${restSeconds} s rest between sets" else null

    /** Work plus rest between sets; a reps set is timed at about three seconds a rep. */
    val estimatedSeconds: Int get() {
        val oneSide = reps?.let { it.last * SECONDS_PER_REP } ?: (seconds ?: 0)
        val work = if (exercise.perSide) oneSide * 2 else oneSide
        return sets * work + (sets - 1) * restSeconds
    }

    companion object {
        const val SECONDS_PER_REP = 3
        fun formatSeconds(s: Int) = if (s >= 60 && s % 60 == 0) "${s / 60} min" else "$s s"
    }
}

enum class SessionFocus(val label: String) {
    FULL_BODY_A("Full body"), FULL_BODY_B("Full body"), UPPER("Upper body"), LOWER("Lower body"),
    CONDITIONING("Conditioning & core"), RECOVERY("Recovery")
}

/**
 * What the plan is built from. A null field was never provided, so the generator uses a safe
 * default and says so in [DailyWorkout.basis] rather than presenting it as the user's choice.
 */
data class TrainingInputs(
    val goal: FitnessGoal? = null,
    val daysPerWeek: Int? = null,
    val experience: Experience? = null,
    val sessionMinutes: Int? = null,
    val equipment: Equipment? = null
)

data class DailyWorkout(
    val date: LocalDate,
    val focus: SessionFocus,
    val isRestDay: Boolean,
    val sessionMinutes: Int,
    val moves: List<PlannedMove>,
    val estimatedSeconds: Int,
    /** Plain-language lines on which inputs came from the profile and which are defaults. */
    val basis: List<String>
) {
    val title get() = if (isRestDay) "Recovery day" else "${focus.label} workout"
    val estimatedMinutes get() = (estimatedSeconds + 59) / 60
    fun phase(phase: WorkoutPhase) = moves.filter { it.phase == phase }
}

/**
 * Small rule-based generator: picks today's session from the weekly schedule, fills exercise slots
 * that suit the goal, experience and equipment, and stops adding work once the time budget is spent.
 * Training days are spread so the same muscles are never trained hard on consecutive days.
 */
object DailyWorkoutGenerator {
    const val DEFAULT_MINUTES = 30
    const val DEFAULT_DAYS = 3
    val DEFAULT_EXPERIENCE = Experience.BEGINNER
    val DEFAULT_EQUIPMENT = Equipment.BODYWEIGHT
    val MINUTE_OPTIONS = listOf(15, 20, 30, 45, 60)

    /** Time to set up and move to the next exercise. */
    const val TRANSITION_SECONDS = 45

    private val schedules = mapOf(
        1 to setOf(DayOfWeek.WEDNESDAY),
        2 to setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
        3 to setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        4 to setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        5 to setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        6 to DayOfWeek.entries.toSet() - DayOfWeek.SUNDAY,
        7 to DayOfWeek.entries.toSet()
    )

    fun trainingDays(daysPerWeek: Int): Set<DayOfWeek> = schedules.getValue(daysPerWeek.coerceIn(1, 7))

    fun focusFor(date: LocalDate, daysPerWeek: Int): SessionFocus {
        val days = daysPerWeek.coerceIn(1, 7)
        val schedule = trainingDays(days)
        if (date.dayOfWeek !in schedule) return SessionFocus.RECOVERY
        val index = schedule.count { it < date.dayOfWeek }
        return when {
            days <= 3 -> if (index % 2 == 0) SessionFocus.FULL_BODY_A else SessionFocus.FULL_BODY_B
            days == 4 -> if (index % 2 == 0) SessionFocus.UPPER else SessionFocus.LOWER
            else -> listOf(SessionFocus.UPPER, SessionFocus.LOWER, SessionFocus.CONDITIONING, SessionFocus.UPPER,
                SessionFocus.LOWER, SessionFocus.CONDITIONING, SessionFocus.RECOVERY)[index]
        }
    }

    private val slots: Map<SessionFocus, List<List<String>>> = run {
        val squat = listOf("barbell_back_squat", "goblet_squat", "leg_press", "bodyweight_squat")
        val hinge = listOf("db_romanian_deadlift", "glute_bridge")
        val singleLeg = listOf("forward_lunge", "step_up", "reverse_lunge")
        val push = listOf("bench_press", "db_floor_press", "push_up", "wall_push_up")
        val shoulder = listOf("db_shoulder_press", "push_up", "wall_push_up")
        val pull = listOf("lat_pulldown", "db_row", "superman")
        val row = listOf("seated_cable_row", "db_row", "superman")
        val core = listOf("plank", "dead_bug", "crunch")
        mapOf(
            SessionFocus.FULL_BODY_A to listOf(squat, push, pull, core, listOf("calf_raise")),
            SessionFocus.FULL_BODY_B to listOf(singleLeg.reversed(), shoulder, hinge, row, listOf("dead_bug", "crunch", "plank")),
            SessionFocus.UPPER to listOf(push, pull, shoulder, row, listOf("db_biceps_curl"), core),
            SessionFocus.LOWER to listOf(squat, hinge, singleLeg, listOf("standing_hamstring_curl"), listOf("calf_raise", "standing_hip_abduction"), core),
            SessionFocus.CONDITIONING to listOf(listOf("mountain_climbers", "jumping_jacks"), listOf("jumping_jacks", "march_in_place"),
                listOf("march_in_place"), core, listOf("dead_bug", "crunch"))
        )
    }

    private val coolDowns = mapOf(
        SessionFocus.FULL_BODY_A to listOf("hamstring_stretch", "chest_stretch"),
        SessionFocus.FULL_BODY_B to listOf("quad_stretch", "back_stretch"),
        SessionFocus.UPPER to listOf("chest_stretch", "back_stretch"),
        SessionFocus.LOWER to listOf("hamstring_stretch", "quad_stretch"),
        SessionFocus.CONDITIONING to listOf("hamstring_stretch", "back_stretch"),
        SessionFocus.RECOVERY to listOf("hamstring_stretch", "quad_stretch", "chest_stretch", "back_stretch")
    )

    fun generate(inputs: TrainingInputs, date: LocalDate): DailyWorkout {
        val days = inputs.daysPerWeek?.coerceIn(1, 7) ?: DEFAULT_DAYS
        val experience = inputs.experience ?: DEFAULT_EXPERIENCE
        val equipment = inputs.equipment ?: DEFAULT_EQUIPMENT
        val minutes = (inputs.sessionMinutes ?: DEFAULT_MINUTES).coerceIn(10, 90)
        val focus = focusFor(date, days)
        val budget = minutes * 60

        val short = minutes < 20
        val coolDown = coolDowns.getValue(focus).let { if (short && focus != SessionFocus.RECOVERY) it.take(1) else it }
            .map { PlannedMove(ExerciseLibrary.require(it), WorkoutPhase.COOL_DOWN, 1, seconds = 30) }
        val moves = if (focus == SessionFocus.RECOVERY) coolDown else {
            val warmUp = listOf(
                PlannedMove(ExerciseLibrary.require("march_in_place"), WorkoutPhase.WARM_UP, 1, seconds = if (short) 60 else 120),
                PlannedMove(ExerciseLibrary.require("arm_circles"), WorkoutPhase.WARM_UP, 1, seconds = if (short) 30 else 60)
            )
            var remaining = budget - (warmUp + coolDown).sumOf { it.estimatedSeconds + TRANSITION_SECONDS }
            val main = mutableListOf<PlannedMove>()
            val used = mutableSetOf<String>()
            for (candidates in slots.getValue(focus)) {
                if (main.size >= maxExercises(experience)) break
                val exercise = candidates.map(ExerciseLibrary::require)
                    .firstOrNull { it.id !in used && it.equipment <= equipment && it.minExperience <= experience } ?: continue
                // Fewer sets is better than dropping the session's first exercise entirely.
                val move = (sets(experience) downTo if (main.isEmpty()) 1 else sets(experience))
                    .map { prescribe(exercise, it, focus, inputs.goal, experience) }
                    .firstOrNull { it.estimatedSeconds + TRANSITION_SECONDS <= remaining } ?: continue
                main += move; used += exercise.id
                remaining -= move.estimatedSeconds + TRANSITION_SECONDS
            }
            val finisher = if (inputs.goal == FitnessGoal.LOSE_WEIGHT && focus != SessionFocus.CONDITIONING)
                finisher(experience, remaining) else null
            warmUp + main + listOfNotNull(finisher) + coolDown
        }
        val estimated = moves.sumOf { it.estimatedSeconds } + (moves.size - 1).coerceAtLeast(0) * TRANSITION_SECONDS
        return DailyWorkout(date, focus, focus == SessionFocus.RECOVERY, minutes, moves, estimated,
            basis(inputs, days, experience, equipment, minutes))
    }

    fun maxExercises(experience: Experience) = when (experience) {
        Experience.BEGINNER -> 4; Experience.INTERMEDIATE -> 5; Experience.ADVANCED -> 6
    }

    fun sets(experience: Experience) = when (experience) {
        Experience.BEGINNER -> 2; Experience.INTERMEDIATE -> 3; Experience.ADVANCED -> 4
    }

    private fun prescribe(exercise: LibraryExercise, sets: Int, focus: SessionFocus, goal: FitnessGoal?,
                          experience: Experience): PlannedMove {
        if (exercise.timed) {
            val conditioning = focus == SessionFocus.CONDITIONING && MuscleGroup.CORE !in exercise.muscles
            val seconds = when (experience) {
                Experience.BEGINNER -> if (conditioning) 30 else 20
                Experience.INTERMEDIATE -> if (conditioning) 40 else 30
                Experience.ADVANCED -> 45
            }
            val rest = if (conditioning) when (experience) { Experience.BEGINNER -> 30; Experience.INTERMEDIATE -> 20; Experience.ADVANCED -> 15 } else 30
            return PlannedMove(exercise, WorkoutPhase.MAIN, sets, seconds = seconds, restSeconds = rest)
        }
        val small = exercise.muscles.all { it in setOf(MuscleGroup.CALVES, MuscleGroup.BICEPS, MuscleGroup.CORE) } ||
            exercise.id in setOf("standing_hip_abduction", "standing_hamstring_curl")
        val (reps, rest) = when {
            small -> 12..15 to 45
            goal == FitnessGoal.GAIN_WEIGHT -> 8..12 to 90
            goal == FitnessGoal.LOSE_WEIGHT -> 12..15 to 45
            else -> 10..12 to 60
        }
        return PlannedMove(exercise, WorkoutPhase.MAIN, sets, reps = reps, restSeconds = rest)
    }

    /** Short intervals for fat loss, only when at least two rounds still fit. */
    private fun finisher(experience: Experience, remaining: Int): PlannedMove? {
        val exercise = ExerciseLibrary.require(if (experience == Experience.BEGINNER) "jumping_jacks" else "mountain_climbers")
        return (4 downTo 2).map { PlannedMove(exercise, WorkoutPhase.FINISHER, it, seconds = 30, restSeconds = 30) }
            .firstOrNull { it.estimatedSeconds + TRANSITION_SECONDS <= remaining }
    }

    private fun basis(inputs: TrainingInputs, days: Int, experience: Experience, equipment: Equipment, minutes: Int) = listOf(
        inputs.goal?.let { "Goal: ${it.displayName} (from your profile)" } ?: "Goal: not set, so this is a general fitness plan",
        inputs.daysPerWeek?.let { "$days training days a week (from your profile)" } ?: "Training days not set; assuming $days a week",
        "${experience.label} level" + if (inputs.experience == null) " (default; change below)" else "",
        "$minutes minutes" + if (inputs.sessionMinutes == null) " (default; change below)" else "",
        "${equipment.label}" + if (inputs.equipment == null) " (default; change below)" else ""
    )
}
