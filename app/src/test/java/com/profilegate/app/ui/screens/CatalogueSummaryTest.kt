package com.profilegate.app.ui.screens

import com.profilegate.app.catalog.CatalogRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The screen this replaced printed "All 10 titles" above a panel that could fit
 * six of them, and nothing in the build knew the two disagreed. The count is
 * checked here instead of trusted, and it is taken from the catalogue rather
 * than written down.
 */
class CatalogueSummaryTest {

    @Test
    fun `the count in the sentence is the real number of titles`() {
        val summary = catalogueSummary(CatalogRepository.titles.size)
        assertTrue(
            "the heading must name the catalogue's own size: $summary",
            "All ${CatalogRepository.titles.size}," in summary,
        )
    }

    @Test
    fun `one title is a sentence, not a count of one`() {
        assertEquals(
            "One title, what it is rated, and what the gate has read in its frame.",
            catalogueSummary(1),
        )
    }

    @Test
    fun `an empty list says so rather than printing a zero`() {
        val summary = catalogueSummary(0)
        assertTrue("an empty list must not read as a quantity: $summary", "0" !in summary)
        assertTrue(summary.startsWith("Nothing"))
    }
}
