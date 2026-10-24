package com.kcalfit.app.domain.activity

import java.text.Normalizer
import java.util.Locale

enum class ActivityCategory(val label: String) {
    CARDIO("Cardio"),
    STRENGTH("Strength Training"),
    SPORTS("Sports"),
    OUTDOOR("Outdoor Activities"),
    MOBILITY("Mobility & Flexibility")
}

/**
 * What the existing tracker can honestly offer for an activity. No activity gets its own
 * algorithm: every live session uses the same timer and the same calorie estimate.
 */
enum class ActivityTracking(val summary: String) {
    /** Live timer and the app's general calorie estimate. */
    TIMER("Live timer · estimated kcal"),
    /** As [TIMER], plus phone steps when device step counting is switched on. */
    TIMER_WITH_STEPS("Live timer · estimated kcal · phone steps when step counting is on"),
    /** The phone is rarely carried or usable during this activity, so it is logged afterwards. */
    LOG_ONLY("Log after your session · live tracking isn't reliable")
}

data class ActivityType(
    val id: String,
    val name: String,
    val category: ActivityCategory,
    val tracking: ActivityTracking = ActivityTracking.TIMER,
    val popular: Boolean = false,
    val aliases: List<String> = emptyList()
) {
    val supportsLiveSession get() = tracking != ActivityTracking.LOG_ONLY
    val tracksSteps get() = tracking == ActivityTracking.TIMER_WITH_STEPS
}

object ActivityCatalog {
    private val C = ActivityCategory.CARDIO
    private val S = ActivityCategory.STRENGTH
    private val SP = ActivityCategory.SPORTS
    private val O = ActivityCategory.OUTDOOR
    private val M = ActivityCategory.MOBILITY
    private val STEPS = ActivityTracking.TIMER_WITH_STEPS
    private val LOG = ActivityTracking.LOG_ONLY

    /** Ordered by how commonly each activity is chosen within its category. */
    val all: List<ActivityType> = listOf(
        ActivityType("walking", "Walking", C, STEPS, true, listOf("walk", "brisk walking", "power walking")),
        ActivityType("running", "Running", C, STEPS, true, listOf("run", "jogging", "jog")),
        ActivityType("treadmill", "Treadmill", C, STEPS, true, listOf("treadmill walking", "treadmill running")),
        ActivityType("cycling", "Cycling", C, popular = true, aliases = listOf("bike", "biking", "bicycle", "road cycling")),
        ActivityType("stationary_cycling", "Stationary Cycling", C, popular = true, aliases = listOf("exercise bike", "indoor cycling", "spin", "spinning")),
        ActivityType("elliptical", "Elliptical", C, popular = true, aliases = listOf("cross trainer")),
        ActivityType("hiit", "HIIT", C, popular = true, aliases = listOf("high intensity interval training", "interval training", "tabata")),
        ActivityType("swimming", "Swimming", C, LOG, true, listOf("swim", "laps", "pool")),
        ActivityType("rowing_machine", "Rowing Machine", C, aliases = listOf("rower", "erg", "indoor rowing", "rowing")),
        ActivityType("stair_climbing", "Stair Climbing", C, STEPS, aliases = listOf("stairs", "stair climber", "stairmaster", "step machine")),
        ActivityType("jump_rope", "Jump Rope", C, aliases = listOf("skipping", "skipping rope")),
        ActivityType("dance", "Dance", C, aliases = listOf("dancing", "zumba", "dance fitness")),
        ActivityType("aerobics", "Aerobics", C, aliases = listOf("step aerobics", "aerobic class")),
        ActivityType("water_aerobics", "Water Aerobics", C, LOG, aliases = listOf("aqua aerobics", "aqua fit")),

        ActivityType("weight_training", "Weight Training", S, popular = true, aliases = listOf("weights", "weightlifting", "lifting", "gym", "resistance training", "dumbbells", "barbell")),
        ActivityType("bodyweight_training", "Bodyweight Training", S, popular = true, aliases = listOf("calisthenics", "push ups", "pull ups", "squats")),
        ActivityType("circuit_training", "Circuit Training", S, aliases = listOf("circuit", "bootcamp")),
        ActivityType("functional_training", "Functional Training", S, aliases = listOf("crossfit", "functional fitness")),
        ActivityType("core_training", "Core Training", S, aliases = listOf("abs", "core", "plank", "sit ups")),
        ActivityType("kettlebell_training", "Kettlebell Training", S, aliases = listOf("kettlebell", "kettlebells")),
        ActivityType("resistance_band_training", "Resistance Band Training", S, aliases = listOf("bands", "resistance bands")),
        ActivityType("powerlifting", "Powerlifting", S, aliases = listOf("deadlift", "bench press", "squat")),

        ActivityType("football", "Football / Soccer", SP, popular = true, aliases = listOf("football", "soccer", "futsal")),
        ActivityType("basketball", "Basketball", SP, popular = true, aliases = listOf("hoops")),
        ActivityType("badminton", "Badminton", SP, popular = true, aliases = listOf("shuttlecock")),
        ActivityType("tennis", "Tennis", SP, popular = true),
        ActivityType("boxing", "Boxing", SP, aliases = listOf("punching bag", "sparring", "boxing workout")),
        ActivityType("cricket", "Cricket", SP),
        ActivityType("table_tennis", "Table Tennis", SP, aliases = listOf("ping pong")),
        ActivityType("volleyball", "Volleyball", SP, aliases = listOf("beach volleyball")),
        ActivityType("martial_arts", "Martial Arts", SP, aliases = listOf("karate", "taekwondo", "judo", "jiu jitsu", "bjj", "mma")),
        ActivityType("kickboxing", "Kickboxing", SP, aliases = listOf("muay thai")),
        ActivityType("squash", "Squash", SP),
        ActivityType("pickleball", "Pickleball", SP),
        ActivityType("golf", "Golf", SP, STEPS),
        ActivityType("hockey", "Hockey", SP, aliases = listOf("field hockey", "ice hockey")),
        ActivityType("baseball", "Baseball / Softball", SP, aliases = listOf("baseball", "softball")),
        ActivityType("rugby", "Rugby", SP),
        ActivityType("netball", "Netball", SP),

        ActivityType("hiking", "Hiking", O, STEPS, true, listOf("hike", "trekking", "hill walking")),
        ActivityType("trail_running", "Trail Running", O, STEPS, aliases = listOf("trail run", "fell running")),
        ActivityType("mountain_biking", "Mountain Biking", O, aliases = listOf("mtb")),
        ActivityType("rock_climbing", "Rock Climbing", O, aliases = listOf("climbing", "bouldering", "indoor climbing")),
        ActivityType("skating", "Skating", O, aliases = listOf("ice skating", "roller skating", "rollerblading", "inline skating")),
        ActivityType("skateboarding", "Skateboarding", O),
        ActivityType("skiing", "Skiing", O, aliases = listOf("downhill skiing", "cross country skiing")),
        ActivityType("snowboarding", "Snowboarding", O),
        ActivityType("horse_riding", "Horse Riding", O, aliases = listOf("equestrian", "horseback riding")),
        ActivityType("gardening", "Gardening", O, aliases = listOf("yard work")),
        ActivityType("kayaking", "Kayaking / Canoeing", O, LOG, aliases = listOf("kayak", "canoe", "canoeing", "paddling")),
        ActivityType("paddleboarding", "Stand-up Paddleboarding", O, LOG, aliases = listOf("sup", "paddleboard")),
        ActivityType("surfing", "Surfing", O, LOG),

        ActivityType("yoga", "Yoga", M, popular = true, aliases = listOf("vinyasa", "hatha", "power yoga")),
        ActivityType("pilates", "Pilates", M, popular = true, aliases = listOf("reformer")),
        ActivityType("stretching", "Stretching", M, popular = true, aliases = listOf("flexibility", "mobility")),
        ActivityType("tai_chi", "Tai Chi", M, aliases = listOf("qigong")),
        ActivityType("barre", "Barre", M),
        ActivityType("foam_rolling", "Foam Rolling", M, aliases = listOf("myofascial release", "recovery"))
    )

    private val byId = all.associateBy { it.id }

    fun byId(id: String): ActivityType? = byId[id]

    val popular: List<ActivityType> get() = all.filter { it.popular }

    /**
     * Matches names and aliases, ignoring case, accents and punctuation. Name matches rank above
     * alias matches, prefix matches above substring matches, and common activities break ties.
     */
    fun search(query: String, category: ActivityCategory? = null): List<ActivityType> {
        val pool = all.filter { category == null || it.category == category }
        val needle = normalize(query)
        if (needle.isEmpty()) return pool
        return pool.mapNotNull { activity -> rank(activity, needle)?.let { activity to it } }
            .sortedWith(compareBy<Pair<ActivityType, Int>> { it.second }.thenBy { !it.first.popular })
            .map { it.first }
    }

    /** Groups for browsing: common activities first, then each category in catalogue order. */
    fun browse(category: ActivityCategory? = null): List<Pair<String, List<ActivityType>>> =
        if (category != null) listOf(category.label to all.filter { it.category == category })
        else listOf("Popular" to popular) + ActivityCategory.entries.map { cat ->
            cat.label to all.filter { it.category == cat && !it.popular }
        }.filter { it.second.isNotEmpty() }

    private fun rank(activity: ActivityType, needle: String): Int? {
        val name = normalize(activity.name)
        val aliases = activity.aliases.map(::normalize)
        return when {
            name.startsWith(needle) -> 0
            name.split(' ').any { it.startsWith(needle) } -> 1
            aliases.any { it.startsWith(needle) || it.split(' ').any { word -> word.startsWith(needle) } } -> 2
            name.contains(needle) -> 3
            aliases.any { it.contains(needle) } -> 4
            else -> null
        }
    }

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
}
