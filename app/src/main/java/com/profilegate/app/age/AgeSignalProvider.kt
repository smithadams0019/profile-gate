package com.profilegate.app.age

import android.content.ContentResolver
import android.net.Uri
import com.profilegate.app.catalog.AgeBand

/**
 * Queries Amazon's documented `GetUserAgeData` ContentProvider, exactly once per
 * session, and never throws. Every failure path — the provider not existing on this
 * device, a security exception, an unparsable band, `FEATURE_NOT_SUPPORTED`,
 * `UNKNOWN`, `CONSENT_NOT_GRANTED`, or an empty string — becomes [AgeSignal.Silent]
 * with a reason, not a crash and not a default guess. See
 * internal research into Fire TV family-profile and age-signal behaviour section 2B for the exact response
 * shape this is built against: `userStatus` of `VERIFIED` / `SUPERVISED` / `UNKNOWN`
 * / `CONSENT_NOT_GRANTED` / empty, plus `ageLower` / `ageUpper` bands of
 * 0-12/13-15/16-17/18+.
 *
 * On this build, on this emulator, there is no Amazon Appstore content provider
 * installed at all, so every call here resolves to [AgeSignal.Silent]. That is the
 * expected, honest result, not a bug: see SPEC.md point 1.
 */
class AgeSignalProvider(private val resolver: ContentResolver) {

    fun query(): AgeSignal {
        return try {
            val cursor = resolver.query(AGE_DATA_URI, null, null, null, null)
                ?: return AgeSignal.Silent("provider returned null cursor (not installed)")

            cursor.use {
                if (!it.moveToFirst()) {
                    return AgeSignal.Silent("provider returned no rows")
                }

                val statusIndex = it.getColumnIndex("userStatus")
                val lowerIndex = it.getColumnIndex("ageLower")

                if (statusIndex < 0) {
                    return AgeSignal.Silent("response had no userStatus column")
                }

                val status = it.getString(statusIndex)
                if (status != "SUPERVISED") {
                    return AgeSignal.Silent("userStatus was '${status.orEmpty()}', not SUPERVISED")
                }

                val lower = if (lowerIndex >= 0) it.getInt(lowerIndex) else -1
                val band = bandForLowerAge(lower)
                    ?: return AgeSignal.Silent("SUPERVISED but ageLower ($lower) did not map to a band")

                AgeSignal.Verified(band)
            }
        } catch (e: SecurityException) {
            AgeSignal.Silent("SecurityException: ${e.message}")
        } catch (e: IllegalArgumentException) {
            AgeSignal.Silent("provider not found: ${e.message}")
        } catch (e: Exception) {
            AgeSignal.Silent("unexpected error: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun bandForLowerAge(lower: Int): AgeBand? = when (lower) {
        0 -> AgeBand.AGE_0_12
        13 -> AgeBand.AGE_13_15
        16 -> AgeBand.AGE_16_17
        18 -> AgeBand.AGE_18_PLUS
        else -> null
    }

    companion object {
        /** The documented address of Amazon's age API. See SPEC.md point 1. */
        const val AGE_DATA_URI_STRING = "content://amzn_appstore/getUserAgeData"

        /**
         * Parsed on first use rather than at class initialisation.
         *
         * `Uri.parse` is a real Android call, so holding the parsed value in a
         * `val` made merely *loading* this class depend on the Android runtime.
         * Anything referencing it from a JVM unit test — including
         * [com.profilegate.app.AppController], which every coverage test builds —
         * died in its static initialiser with "Method parse in android.net.Uri
         * not mocked", which names neither this class nor a Uri.
         */
        val AGE_DATA_URI: Uri by lazy { Uri.parse(AGE_DATA_URI_STRING) }
    }
}
