package com.kcalfit.app.domain.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityCatalogTest {
    private val catalog = ActivityCatalog.all

    @Test fun idsAndNormalizedNamesAreUnique() {
        assertEquals(catalog.size, catalog.map { it.id }.toSet().size)
        assertEquals(catalog.size, catalog.map { ActivityCatalog.normalize(it.name) }.toSet().size)
    }

    @Test fun noAliasIsAnotherActivitysName() {
        val names = catalog.associate { ActivityCatalog.normalize(it.name) to it.id }
        catalog.forEach { activity ->
            activity.aliases.forEach { alias ->
                val owner = names[ActivityCatalog.normalize(alias)]
                assertTrue("${activity.id} alias '$alias' duplicates $owner", owner == null || owner == activity.id)
            }
        }
    }

    @Test fun requestedActivitiesAreAvailable() {
        listOf("walking", "running", "treadmill", "cycling", "stationary_cycling", "elliptical", "rowing_machine",
            "swimming", "hiking", "stair_climbing", "jump_rope", "hiit", "weight_training", "bodyweight_training",
            "yoga", "pilates", "stretching", "dance", "boxing", "badminton", "basketball", "football", "tennis")
            .forEach { assertTrue("missing $it", ActivityCatalog.byId(it) != null) }
    }

    @Test fun everyCategoryHasActivities() {
        ActivityCategory.entries.forEach { category -> assertTrue(catalog.any { it.category == category }) }
    }

    @Test fun searchRanksNamePrefixFirst() {
        assertEquals("running", ActivityCatalog.search("run").first().id)
        assertEquals("walking", ActivityCatalog.search("walk").first().id)
    }

    @Test fun searchMatchesAliasesCaseAndAccentInsensitively() {
        assertEquals("football", ActivityCatalog.search("SOCCER").first().id)
        assertEquals("table_tennis", ActivityCatalog.search("ping pong").first().id)
        assertEquals("yoga", ActivityCatalog.search("  Yóga ").first().id)
        assertEquals("stationary_cycling", ActivityCatalog.search("spin").first().id)
        assertEquals("paddleboarding", ActivityCatalog.search("stand up").first().id)
    }

    @Test fun searchWithinCategoryOnlyReturnsThatCategory() {
        val results = ActivityCatalog.search("ing", ActivityCategory.OUTDOOR)
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category == ActivityCategory.OUTDOOR })
    }

    @Test fun unmatchedSearchIsEmpty() {
        assertTrue(ActivityCatalog.search("zzzz").isEmpty())
        assertTrue(ActivityCatalog.search("yoga", ActivityCategory.SPORTS).isEmpty())
    }

    @Test fun blankSearchReturnsWholePool() {
        assertEquals(catalog, ActivityCatalog.search("   "))
    }

    @Test fun browseShowsPopularFirstAndEveryActivityExactlyOnce() {
        val groups = ActivityCatalog.browse()
        assertEquals("Popular", groups.first().first)
        val listed = groups.flatMap { it.second }.map { it.id }
        assertEquals(listed.size, listed.toSet().size)
        assertEquals(catalog.map { it.id }.toSet(), listed.toSet())
    }

    @Test fun browseCategoryIncludesItsPopularActivities() {
        val (title, activities) = ActivityCatalog.browse(ActivityCategory.MOBILITY).single()
        assertEquals(ActivityCategory.MOBILITY.label, title)
        assertTrue(activities.any { it.id == "yoga" })
    }

    @Test fun trackingCapabilitiesAreHonest() {
        assertTrue(ActivityCatalog.byId("walking")!!.tracksSteps)
        assertFalse(ActivityCatalog.byId("weight_training")!!.tracksSteps)
        assertFalse(ActivityCatalog.byId("cycling")!!.tracksSteps)
        assertFalse(ActivityCatalog.byId("swimming")!!.supportsLiveSession)
        assertTrue(ActivityCatalog.byId("yoga")!!.supportsLiveSession)
    }
}
