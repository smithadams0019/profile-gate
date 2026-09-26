package com.profilegate.app.catalog

data class Title(
    val id: String,
    val name: String,
    val band: AgeBand,
    /** Short line shown under the tile, so a judge can see why a title is above band. */
    val note: String,
)

/**
 * MOCK DATA. Nine invented titles with invented age bands, standing in for a real
 * content partner's metadata. No real title, studio, or Amazon catalogue entry is
 * used anywhere in this app. This is deliberate: the app can never inspect another
 * app's video (internal research notes on Fire TV / Fire OS platform facts, Fire TV / Vega section), so it works on
 * a title record instead, the same surface every real incumbent uses, and adds the
 * remote-behaviour check none of them do. See SPEC.md, "Data".
 */
object CatalogRepository {
    val titles: List<Title> = listOf(
        Title("t1", "Puddle Friends", AgeBand.AGE_0_12, "Toddler sing-along, 6 minutes"),
        Title("t2", "Story Barn", AgeBand.AGE_0_12, "Picture-book read-alongs"),
        Title("t3", "Nebula Cadets S1E3", AgeBand.AGE_0_12, "Gentle space-explorer cartoon"),
        Title("t4", "Counting Tugboats", AgeBand.AGE_0_12, "Numbers and shapes, 4 minutes"),
        Title("t5", "Backyard Builders", AgeBand.AGE_0_12, "Kids building a treehouse"),
        Title(
            id = "t6",
            name = "Nebula Cadets: Lockdown Special",
            band = AgeBand.AGE_13_15,
            note = "Mislabelled into the kids row. Rated 13-15 for threat and peril.",
        ),
        Title(
            id = "t7",
            name = "Midnight Garage",
            band = AgeBand.AGE_16_17,
            note = "A recommended-row leak. Rated 16-17 for language.",
        ),
        Title(
            id = "t8",
            name = "Late Edition",
            band = AgeBand.AGE_18_PLUS,
            note = "Adult drama surfaced by a broken row filter.",
        ),
        Title("t9", "Puddle Friends: Bedtime", AgeBand.AGE_0_12, "A calmer, quieter episode"),
        Title(
            id = "t10",
            name = "Sunny Meadow Friends",
            band = AgeBand.AGE_0_12,
            note = "Declared 0-12 like the rest of this row. Its own frame disagrees " +
                "with its own label, exactly the Disney MFK failure this app exists for.",
        ),
    )

    fun byId(id: String): Title? = titles.firstOrNull { it.id == id }
}
