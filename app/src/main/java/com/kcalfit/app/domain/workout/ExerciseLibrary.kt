package com.kcalfit.app.domain.workout

/** Ordered from least to most kit; a level includes everything below it. */
enum class Equipment(val label: String) { BODYWEIGHT("Bodyweight"), DUMBBELLS("Dumbbells"), GYM("Full gym") }

enum class Experience(val label: String) { BEGINNER("Beginner"), INTERMEDIATE("Intermediate"), ADVANCED("Advanced") }

enum class MuscleGroup(val label: String) {
    QUADS("Quads"), GLUTES("Glutes"), HAMSTRINGS("Hamstrings"), CALVES("Calves"), CHEST("Chest"),
    BACK("Back"), LOWER_BACK("Lower back"), SHOULDERS("Shoulders"), BICEPS("Biceps"), TRICEPS("Triceps"),
    CORE("Core"), FULL_BODY("Full body")
}

/**
 * One exercise. [timed] exercises are held or done for seconds (plank, march); the rest are counted
 * in reps. [perSide] exercises are done once for each leg or arm, so a set takes twice as long.
 * [demo] names a bundled animation of this exact movement, or is null when there is none; ExerciseMedia
 * then picks a related animation, so the UI always has one to show.
 */
data class LibraryExercise(
    val id: String,
    val name: String,
    val muscles: List<MuscleGroup>,
    val equipment: Equipment,
    val equipmentNote: String,
    val steps: List<String>,
    val cues: List<String>,
    val minExperience: Experience = Experience.BEGINNER,
    val timed: Boolean = false,
    val perSide: Boolean = false,
    val demo: String? = null
)

object ExerciseLibrary {
    private fun ex(id: String, name: String, muscles: List<MuscleGroup>, equipment: Equipment, note: String,
                   steps: List<String>, cues: List<String>, min: Experience = Experience.BEGINNER,
                   timed: Boolean = false, perSide: Boolean = false, demo: String? = null) =
        LibraryExercise(id, name, muscles, equipment, note, steps, cues, min, timed, perSide, demo)

    private val B = Equipment.BODYWEIGHT
    private val D = Equipment.DUMBBELLS
    private val G = Equipment.GYM

    val all: List<LibraryExercise> = listOf(
        // Warm-up and conditioning
        ex("march_in_place", "March in place", listOf(MuscleGroup.FULL_BODY), B, "None",
            listOf("Stand tall and march, lifting each knee towards hip height.", "Swing your arms in time with your legs."),
            listOf("Breathe steadily.", "Build the pace gradually."), timed = true),
        ex("arm_circles", "Arm circles", listOf(MuscleGroup.SHOULDERS), B, "None",
            listOf("Stand with arms straight out to the sides.", "Make small circles, growing larger; reverse halfway."),
            listOf("Keep your shoulders down, away from your ears."), timed = true),
        ex("jumping_jacks", "Jumping jacks", listOf(MuscleGroup.FULL_BODY), B, "None",
            listOf("Jump your feet out while raising your arms overhead.", "Jump back to the start and repeat."),
            listOf("Land softly on the balls of your feet.", "Step out instead of jumping for a low-impact version."), timed = true, demo = "jumping_jacks"),
        ex("mountain_climbers", "Mountain climbers", listOf(MuscleGroup.CORE, MuscleGroup.SHOULDERS), B, "None",
            listOf("Start in a high plank with hands under shoulders.", "Drive one knee towards your chest, then switch legs."),
            listOf("Keep your hips level with your shoulders."), min = Experience.INTERMEDIATE, timed = true),
        // Lower body
        ex("bodyweight_squat", "Bodyweight squat", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), B, "A chair behind you (optional)",
            listOf("Stand with feet shoulder-width apart, arms out in front.", "Sit your hips back and down as if towards a chair.", "Push through your heels to stand."),
            listOf("Knees track over your toes.", "Keep your chest up and back flat."), demo = "squat"),
        ex("forward_lunge", "Forward lunge", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), B, "A rail or wall for balance (optional)",
            listOf("Stand tall, holding a support if needed.", "Step forward and lower until both knees bend to about 90°.", "Push back to the start; alternate legs."),
            listOf("Front knee stays over the ankle.", "Keep your torso upright."), perSide = true, demo = "lunge"),
        ex("reverse_lunge", "Reverse lunge", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), B, "None",
            listOf("Stand tall with feet hip-width apart.", "Step one foot back and lower your back knee towards the floor.", "Drive through the front heel to return; alternate legs."),
            listOf("Keep most of your weight on the front leg.", "Move slowly and stay balanced."), perSide = true),
        ex("step_up", "Step-up", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), B, "A sturdy step or bottom stair",
            listOf("Place one foot fully on the step.", "Press through that foot to stand up on the step.", "Step down with control; switch legs halfway."),
            listOf("Use a handrail if you need balance.", "Don't push off the bottom foot."), perSide = true, demo = "step_up"),
        ex("glute_bridge", "Glute bridge", listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), B, "A mat",
            listOf("Lie on your back, knees bent, feet flat.", "Squeeze your glutes and lift your hips until knees, hips and shoulders line up.", "Lower slowly."),
            listOf("Don't arch your lower back at the top.")),
        ex("standing_hamstring_curl", "Standing hamstring curl", listOf(MuscleGroup.HAMSTRINGS), B, "A chair for balance; ankle weight optional",
            listOf("Hold the back of a chair.", "Bend one knee, bringing your heel towards your buttocks.", "Lower slowly; switch legs."),
            listOf("Keep your thighs side by side.", "Move slowly in both directions."), perSide = true, demo = "knee_curl"),
        ex("standing_hip_abduction", "Standing hip abduction", listOf(MuscleGroup.GLUTES), B, "A chair for balance",
            listOf("Stand behind a chair holding it lightly.", "Lift one straight leg out to the side.", "Lower slowly; switch legs."),
            listOf("Keep your toes facing forward.", "Don't lean to the side."), perSide = true, demo = "hip_abduction"),
        ex("calf_raise", "Standing calf raise", listOf(MuscleGroup.CALVES), B, "A chair or wall for balance",
            listOf("Stand tall holding a support.", "Rise onto the balls of your feet.", "Lower your heels slowly."),
            listOf("Pause briefly at the top."), demo = "toe_stand"),
        ex("goblet_squat", "Goblet squat", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), D, "One dumbbell",
            listOf("Hold a dumbbell vertically against your chest.", "Squat down between your heels, chest up.", "Drive up through your heels."),
            listOf("Elbows inside your knees at the bottom.", "Keep your heels down.")),
        ex("db_romanian_deadlift", "Dumbbell Romanian deadlift", listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES), D, "Two dumbbells",
            listOf("Stand holding dumbbells in front of your thighs.", "Push your hips back, sliding the weights down your legs.", "Squeeze your glutes to stand."),
            listOf("Keep a flat back and soft knees.", "Stop when you feel a strong hamstring stretch.")),
        ex("barbell_back_squat", "Barbell back squat", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), G, "Barbell and squat rack",
            listOf("Set the bar on your upper back and step out of the rack.", "Squat to at least parallel.", "Drive up, keeping the bar over mid-foot."),
            listOf("Brace your core before each rep.", "Use safety pins."), min = Experience.INTERMEDIATE),
        ex("leg_press", "Leg press", listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES), G, "Leg press machine",
            listOf("Sit with feet shoulder-width on the platform.", "Lower until knees reach about 90°.", "Press back without locking your knees."),
            listOf("Keep your lower back against the pad.")),
        // Upper-body push
        ex("wall_push_up", "Wall push-up", listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), B, "A wall",
            listOf("Stand an arm's length from a wall, hands on it at shoulder height.", "Bend your elbows to bring your chest towards the wall.", "Push back to the start."),
            listOf("Keep your body in one straight line.", "Step further back to make it harder."), demo = "wallpushup"),
        ex("push_up", "Push-up", listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), B, "A mat",
            listOf("Start in a high plank, hands slightly wider than shoulders.", "Lower your chest towards the floor.", "Push back up."),
            listOf("Elbows at about 45° from your body.", "Drop to your knees if form breaks."), min = Experience.INTERMEDIATE, demo = "push_up"),
        ex("db_floor_press", "Dumbbell floor press", listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), D, "Two dumbbells and a mat",
            listOf("Lie on your back, knees bent, dumbbells above your chest.", "Lower until your upper arms touch the floor.", "Press back up."),
            listOf("Wrists stay straight over your elbows."), demo = "chest_press"),
        ex("db_shoulder_press", "Seated dumbbell shoulder press", listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS), D, "Two dumbbells and a chair",
            listOf("Sit tall with dumbbells at shoulder height.", "Press them overhead until your arms are straight.", "Lower slowly."),
            listOf("Don't arch your back.", "Keep your core engaged."), demo = "overhead_press"),
        ex("bench_press", "Barbell bench press", listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), G, "Bench, barbell and a spotter or safeties",
            listOf("Lie on the bench, eyes under the bar.", "Lower the bar to mid-chest.", "Press back up over your shoulders."),
            listOf("Feet planted, shoulder blades squeezed together."), min = Experience.INTERMEDIATE, demo = "bench_press"),
        // Upper-body pull
        ex("superman", "Superman back extension", listOf(MuscleGroup.LOWER_BACK, MuscleGroup.GLUTES), B, "A mat",
            listOf("Lie face down with arms stretched forward.", "Lift your arms, chest and legs a few centimetres.", "Hold briefly, then lower."),
            listOf("Look at the floor to keep your neck neutral.", "Small, controlled lifts."), demo = "floor_back_extensions"),
        ex("db_row", "One-arm dumbbell row", listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), D, "One dumbbell and a bench or chair",
            listOf("Brace one hand and knee on a bench, back flat.", "Pull the dumbbell towards your hip.", "Lower with control; switch sides."),
            listOf("Lead with your elbow.", "Don't twist your torso."), perSide = true),
        ex("db_biceps_curl", "Dumbbell biceps curl", listOf(MuscleGroup.BICEPS), D, "Two dumbbells",
            listOf("Sit tall or stand, holding dumbbells at your sides, palms forward.", "Curl them towards your shoulders.", "Lower slowly."),
            listOf("Keep your elbows by your sides.", "No swinging."), demo = "biceps_curl"),
        ex("lat_pulldown", "Lat pulldown", listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), G, "Cable pulldown machine",
            listOf("Grip the bar slightly wider than shoulders.", "Pull it to your upper chest.", "Return with control."),
            listOf("Lean back slightly; don't swing."), demo = "lat_pulldown"),
        ex("seated_cable_row", "Seated cable row", listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), G, "Cable row machine",
            listOf("Sit tall with knees slightly bent.", "Pull the handle to your lower ribs.", "Return slowly."),
            listOf("Squeeze your shoulder blades together."), demo = "seated_row"),
        // Core
        ex("crunch", "Crunch", listOf(MuscleGroup.CORE), B, "A mat",
            listOf("Lie on your back, knees bent, hands behind your head.", "Curl your shoulders off the floor.", "Lower slowly."),
            listOf("Don't pull on your neck.", "Breathe out as you curl up."), demo = "abdominal_curl"),
        ex("plank", "Plank", listOf(MuscleGroup.CORE), B, "A mat",
            listOf("Rest on your forearms and toes.", "Hold your body in one straight line."),
            listOf("Squeeze your glutes; don't let your hips sag.", "Drop to your knees if needed."), timed = true),
        ex("dead_bug", "Dead bug", listOf(MuscleGroup.CORE), B, "A mat",
            listOf("Lie on your back, arms up, knees bent at 90°.", "Lower the opposite arm and leg towards the floor.", "Return and switch sides."),
            listOf("Keep your lower back pressed to the floor.")),
        // Cool-down stretches
        ex("hamstring_stretch", "Hamstring stretch", listOf(MuscleGroup.HAMSTRINGS), B, "A chair or bench",
            listOf("Sit on the edge of a chair, one leg straight with heel on the floor.", "Lean forward from your hips until you feel a stretch.", "Hold, then switch legs."),
            listOf("Keep your back straight.", "Stretch gently; never bounce."), perSide = true, timed = true, demo = "hamstring_stretch"),
        ex("quad_stretch", "Quad stretch", listOf(MuscleGroup.QUADS), B, "A chair or wall for balance",
            listOf("Hold a support with one hand.", "Bend one knee and hold that ankle behind you.", "Hold, then switch legs."),
            listOf("Keep your knees close together."), perSide = true, timed = true, demo = "quad_stretch"),
        ex("chest_stretch", "Chest stretch", listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS), B, "None",
            listOf("Clasp your hands behind your back.", "Gently lift your arms and open your chest."),
            listOf("Stand tall; don't lean forward."), timed = true, demo = "chest_stretch"),
        ex("back_stretch", "Back stretch", listOf(MuscleGroup.BACK), B, "None",
            listOf("Reach both arms forward and round your upper back.", "Hold, breathing slowly."),
            listOf("Relax your shoulders."), timed = true, demo = "backstretch")
    )

    private val byId = all.associateBy { it.id }
    private val byName = all.associateBy { it.name.lowercase() }

    /** Older workouts and the weekly Training Plan name some exercises differently from this library. */
    private val aliases = mapOf(
        "dumbbell row" to "db_row",
        "shoulder press" to "db_shoulder_press",
        "romanian deadlift" to "db_romanian_deadlift",
        "bench press" to "bench_press",
        "push-ups" to "push_up",
        "squats" to "barbell_back_squat",
        "lunges" to "forward_lunge",
        "calf raises" to "calf_raise",
        "overhead press" to "db_shoulder_press",
        "leg curls" to "standing_hamstring_curl",
        "planks" to "plank",
        "crunches" to "crunch"
    )

    fun byId(id: String): LibraryExercise? = byId[id]

    fun find(name: String): LibraryExercise? {
        val key = name.trim().lowercase()
        return byName[key] ?: aliases[key]?.let(byId::get)
    }

    fun require(id: String): LibraryExercise = requireNotNull(byId[id]) { "Unknown exercise $id" }

    /** Every exact-movement animation the library names; the UI maps each key to a raw resource. */
    val bundledDemos: Set<String> = all.mapNotNull { it.demo }.toSet()
}
