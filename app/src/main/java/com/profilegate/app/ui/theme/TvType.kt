package com.profilegate.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.profilegate.app.R

/**
 * IBM Plex Sans, version 3.201, IBM Corp., SIL Open Font License 1.1. Four static
 * weights cut from the Google Fonts variable master at wdth 100 — see
 * ATTRIBUTION.md for how, and for the licence.
 *
 * **It is here for the figures.** Every screen in this app is timestamps, counts
 * and percentages, and a record whose numbers do not line up in a column is not
 * behaving like a record. Plex's lining figures are tabular by default: all ten
 * digits advance 600 of 1000 units per em, in all four weights, measured out of
 * the shipped files rather than taken from a specimen page. So "0-12" and "13-15"
 * rule straight down the right-hand edge of the title list, and 14:55:12 sits
 * under 14:55:09 in the household log with the colons in the same place. No
 * `tnum` is requested anywhere, because none is needed.
 *
 * Roboto, which this app rendered in until now, does not do that — and Roboto was
 * never chosen, it is what an Android app looks like when nobody picks a typeface.
 * Plex is also a face drawn for a technical document, which is what this app is,
 * and it shares nothing with the other two Fire TV apps in this set: Steady's
 * Libre Franklin is a warm Franklin Gothic, Simple Mode's Atkinson Hyperlegible is
 * a low-vision face with deliberately disambiguated letterforms.
 */
val PlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold),
)

/**
 * The type scale, which is this app's own and not the reference document's.
 *
 * **What it used to be, and why that was wrong.** 56 / 40 / 32 / 28 / 24 / 20,
 * with a comment citing the shared Fire TV craft reference as the source. Steady ships the
 * identical six numbers with the identical citation, and Simple Mode ships the
 * same ladder one step up. the shared Fire TV craft reference states a floor — 20sp, with body wanting 24 —
 * and three apps read a floor as a specification. Three products that all set a
 * screen title at 40sp/Bold and a lead at 28sp/Medium look related whatever colour
 * they are, and that, more than any single screen, is what a 5-out-of-10 for the
 * set was measuring.
 *
 * **What it is now, and what it follows from.** A ladder of six evenly-spaced
 * steps is what you build when you do not know what the content is. This app's
 * content is known: one measured number, and a lot of near-equal rows of figures
 * underneath it. So the scale is bimodal rather than even — [Display] stands almost
 * two to one over everything else, then [Title] through [Body] sit in a tight
 * cluster inside a single 10sp span, then [Meta] at the floor:
 *
 * ```
 * 64 ────────────────────────────────  the measured number
 * 34 ──────────                        heading
 * 29 ────────                          section
 * 26 ───────                           lead
 * 24 ──────                            body
 * 20 ────                              the floor
 * ```
 *
 * The compressed cluster is the point. On a record, a section heading is not an
 * announcement, it is a label on a column, and it has no business being twice the
 * size of what it labels. Hierarchy inside the cluster is carried by weight and by
 * the rules between blocks, not by another 8sp.
 *
 * Nothing goes below 20sp: at density 320 and three metres that is 18.5 arcminutes
 * of cap height, the hard floor from internal research notes on Fire TV / Fire OS platform facts. Line heights are
 * tighter than the old scale's throughout — 1.03 on the figure, about 1.18 on the
 * two headings — because a set of figures that is leaded like running prose stops
 * reading as a set.
 *
 * Weight never drops below Medium: thin strokes are the first thing room light
 * erases. [Title] and [Heading] moved from Bold to SemiBold, which is a decision
 * rather than a saving — a record labels its columns, it does not shout them.
 */
object TvType {
    /**
     * The one measured number a screen is about, and the only thing in this app
     * allowed to be large. Bold, negatively tracked, and leaded almost solid, so
     * it reads as a figure on a dial rather than as a headline.
     */
    val Display = TextStyle(
        fontFamily = PlexSans,
        fontSize = 64.sp,
        lineHeight = 66.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp,
    )

    /** Screen title. Always top-left, always the same place. */
    val Title = TextStyle(
        fontFamily = PlexSans,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** Section heading, primary button label. */
    val Heading = TextStyle(
        fontFamily = PlexSans,
        fontSize = 29.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** The sentence that carries the screen. */
    val Lead = TextStyle(
        fontFamily = PlexSans,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Medium,
    )

    /** Everything else. */
    val Body = TextStyle(
        fontFamily = PlexSans,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
    )

    /**
     * Timestamps, counts, legal. The hard floor, tracked a third of a point open
     * so a column of figures at this size does not set solid.
     */
    val Meta = TextStyle(
        fontFamily = PlexSans,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.3.sp,
    )
}
