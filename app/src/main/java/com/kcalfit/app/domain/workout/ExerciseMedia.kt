package com.kcalfit.app.domain.workout

/** Who made a bundled animation and under which licence; shown under the animation. */
data class DemoCredit(val author: String, val license: String, val source: String)

/**
 * The animation to show for an exercise. [specific] is false when the clip shows a closely related
 * movement instead of the exercise itself, and the UI says so.
 */
data class DemoMatch(val key: String, val specific: Boolean)

/**
 * Maps every exercise to a bundled animation, so none is ever shown without one. Library exercises
 * use their own [LibraryExercise.demo], then a related clip, then one chosen by muscle group. Plan
 * names outside the library (the weekly Training Plan, the photo workouts) resolve through aliases
 * and keywords, ending in a general warm-up clip.
 */
object ExerciseMedia {
    private const val CDC = "U.S. Centers for Disease Control and Prevention"
    private const val COMMONS = "https://commons.wikimedia.org/wiki/File:"
    private fun cdc(file: String) = DemoCredit(CDC, "Public domain", COMMONS + file)
    private fun commons(author: String, license: String, file: String) = DemoCredit(author, license, COMMONS + file)

    val credits: Map<String, DemoCredit> = mapOf(
        "squat" to cdc("Squat-CDC_strength_training_for_older_adults.gif"),
        "lunge" to cdc("Lunge-CDC_strength_training_for_older_adults.gif"),
        "step_up" to cdc("Step_up-CDC_strength_training_for_older_adults.gif"),
        "knee_curl" to cdc("Knee_curl-CDC_strength_training_for_older_adults.gif"),
        "hip_abduction" to cdc("Hip_abduction-CDC_strength_training_for_older_adults.gif"),
        "toe_stand" to cdc("Toe_stand-CDC_strength_training_for_older_adults.gif"),
        "wallpushup" to cdc("Wallpushup-CDC_strength_training_for_older_adults.gif"),
        "chest_press" to cdc("Chest_press-CDC_strength_training_for_older_adults.gif"),
        "overhead_press" to cdc("Overhead_press-CDC_strength_training_for_older_adults.gif"),
        "floor_back_extensions" to cdc("Floor_back_extensions-CDC_strength_training_for_older_adults.gif"),
        "biceps_curl" to cdc("Biceps_curl-CDC_strength_training_for_older_adults.gif"),
        "abdominal_curl" to cdc("Abdominal_curl-CDC_strength_training_for_older_adults.gif"),
        "hamstring_stretch" to cdc("Hamstring_stretch-CDC_strength_training_for_older_adults.gif"),
        "quad_stretch" to cdc("Quad_stretch-CDC_strength_training_for_older_adults.gif"),
        "chest_stretch" to cdc("Chest_stretch-CDC_strength_training_for_older_adults.gif"),
        "backstretch" to cdc("Backstretch-CDC_strength_training_for_older_adults.gif"),
        "air_squat" to commons("Wensceslao", "CC BY-SA 4.0", "Squats.gif"),
        "jumping_jacks" to commons("Wensceslao", "CC BY-SA 4.0", "Jumpingjacks.gif"),
        "high_knees" to commons("Wensceslao", "CC BY-SA 4.0", "High_knees.gif"),
        "burpee" to commons("Wensceslao", "CC BY-SA 4.0", "Burpees.gif"),
        "push_up" to commons("Wensceslao", "CC BY-SA 4.0", "Pushups.gif"),
        "sit_up" to commons("Wensceslao", "CC BY-SA 4.0", "Situps.gif"),
        "leg_raise" to commons("Wensceslao", "CC BY-SA 4.0", "Legraises.gif"),
        "jump_rope" to commons("Wensceslao", "CC BY-SA 4.0", "Jumpingrope.gif"),
        "dumbbell_fly" to commons("Mohamed Ouda", "CC BY-SA 3.0", "DumbbellFlye.gif"),
        "bench_press" to commons("Mohamed Ouda", "CC BY-SA 3.0", "SmithMachineBenchPress.gif"),
        "pull_up" to commons("Extremistpullup", "CC BY-SA 3.0", "Pullup.gif"),
        "seated_row" to commons("GyorgyGajdos", "CC BY-SA 4.0", "Bent_over_rows_with_resistance_bands_01.gif"),
        "lat_pulldown" to commons("Everkinetic", "CC BY-SA 3.0", "Wide-grip-lat-pull-down-1.gif"),
        "deadlift" to commons("Everkinetic", "CC BY-SA 3.0", "One-arm-side-deadlift-1.gif"),
        "bridge" to commons("J. Lunau", "CC BY-SA 3.0", "Five_tibetan_rite_4.gif"),
        "walking" to commons("VideoPlasty", "CC BY-SA 4.0", "Man_Walking_GIF_Animation_Loop.gif"),
        "running" to commons("VideoPlasty", "CC BY-SA 4.0", "Man_Running_GIF_Animation_Loop.gif"),
        "exercise_bike" to commons("VideoPlasty", "CC BY-SA 4.0", "Man_on_an_Exercise_Bike_GIF_Animation_Loop.gif"),
        "yoga" to commons("VideoPlasty", "CC BY-SA 4.0", "Man_Doing_Yoga_GIF_Animation_Loop.gif"),
        "warm_up" to commons("VideoPlasty", "CC BY-SA 4.0", "Man_Doing_Warm_Up_Exercise_GIF_Animation_Loop.gif")
    )

    /** Shown when nothing closer is known. */
    const val GENERIC = "warm_up"

    /** Library exercises with no clip of their own; each borrows the closest related movement. */
    private val related = mapOf(
        "march_in_place" to "high_knees", "arm_circles" to "warm_up", "mountain_climbers" to "high_knees",
        "reverse_lunge" to "lunge", "glute_bridge" to "bridge", "goblet_squat" to "squat",
        "db_romanian_deadlift" to "deadlift", "barbell_back_squat" to "air_squat", "leg_press" to "squat",
        "db_row" to "seated_row", "plank" to "push_up", "dead_bug" to "leg_raise"
    )

    private val byMuscle = mapOf(
        MuscleGroup.QUADS to "squat", MuscleGroup.GLUTES to "squat", MuscleGroup.HAMSTRINGS to "deadlift",
        MuscleGroup.CALVES to "toe_stand", MuscleGroup.CHEST to "push_up", MuscleGroup.BACK to "seated_row",
        MuscleGroup.LOWER_BACK to "floor_back_extensions", MuscleGroup.SHOULDERS to "overhead_press",
        MuscleGroup.BICEPS to "biceps_curl", MuscleGroup.TRICEPS to "push_up", MuscleGroup.CORE to "sit_up",
        MuscleGroup.FULL_BODY to "warm_up"
    )

    /** Plan names that aren't library exercises, normalised with [normalize]. */
    private val named = mapOf(
        "dumbbell flyes" to DemoMatch("dumbbell_fly", true), "cable crossover" to DemoMatch("dumbbell_fly", false),
        "incline press" to DemoMatch("bench_press", false), "pull-ups" to DemoMatch("pull_up", true),
        "barbell rows" to DemoMatch("seated_row", false), "deadlift" to DemoMatch("deadlift", false),
        "lateral raises" to DemoMatch("overhead_press", false), "russian twists" to DemoMatch("sit_up", false),
        "leg raises" to DemoMatch("leg_raise", true), "jump rope" to DemoMatch("jump_rope", true),
        "burpees" to DemoMatch("burpee", true), "hiit intervals" to DemoMatch("burpee", false),
        "running" to DemoMatch("running", true), "easy run" to DemoMatch("running", true),
        "steady pace" to DemoMatch("running", true), "cycling" to DemoMatch("exercise_bike", true),
        "warm-up walk" to DemoMatch("walking", true), "cool-down walk" to DemoMatch("walking", true),
        "balance flow" to DemoMatch("yoga", true), "breathing practice" to DemoMatch("yoga", false),
        "rest and recover" to DemoMatch("yoga", false), "gentle stretch" to DemoMatch("backstretch", false)
    )

    private val keywords = listOf(
        "run|jog|sprint" to "running", "walk" to "walking", "bike|cycl" to "exercise_bike",
        "yoga|breath|balance|meditat" to "yoga", "stretch" to "backstretch", "burpee|hiit" to "burpee",
        "jumping jack" to "jumping_jacks", "rope|skip" to "jump_rope", "pull-?up|chin-?up" to "pull_up",
        "pulldown" to "lat_pulldown", "row" to "seated_row", "deadlift|hinge" to "deadlift",
        "squat|leg press" to "squat", "lunge" to "lunge", "step" to "step_up", "bridge|hip thrust" to "bridge",
        "calf" to "toe_stand", "push-?up" to "push_up", "fly|flye|crossover" to "dumbbell_fly",
        "bench|chest press|incline" to "bench_press", "shoulder|overhead|raise" to "overhead_press",
        "curl" to "biceps_curl", "plank" to "push_up", "crunch|sit-?up|twist|core|ab" to "sit_up",
        "leg raise" to "leg_raise"
    ).map { (pattern, key) -> Regex("\\b($pattern)", RegexOption.IGNORE_CASE) to key }

    fun normalize(name: String) = name.trim().lowercase().replace(Regex("\\s+"), " ")

    fun forExercise(exercise: LibraryExercise): DemoMatch =
        exercise.demo?.let { DemoMatch(it, true) }
            ?: related[exercise.id]?.let { DemoMatch(it, false) }
            ?: DemoMatch(exercise.muscles.firstNotNullOfOrNull(byMuscle::get) ?: GENERIC, false)

    /** Any name a workout can show; never fails, so every exercise has an animation. */
    fun forName(name: String): DemoMatch {
        ExerciseLibrary.find(name)?.let { return forExercise(it) }
        val key = normalize(name)
        named[key]?.let { return it }
        return DemoMatch(keywords.firstOrNull { it.first.containsMatchIn(key) }?.second ?: GENERIC, false)
    }
}
