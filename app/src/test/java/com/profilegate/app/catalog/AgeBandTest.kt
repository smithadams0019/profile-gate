package com.profilegate.app.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgeBandTest {

    @Test
    fun `a band covers content rated at its own level`() {
        assertTrue(AgeBand.AGE_0_12.covers(AgeBand.AGE_0_12))
    }

    @Test
    fun `a band covers content rated below it`() {
        assertTrue(AgeBand.AGE_18_PLUS.covers(AgeBand.AGE_0_12))
        assertTrue(AgeBand.AGE_16_17.covers(AgeBand.AGE_13_15))
    }

    @Test
    fun `a band does not cover content rated above it`() {
        assertFalse(AgeBand.AGE_0_12.covers(AgeBand.AGE_13_15))
        assertFalse(AgeBand.AGE_13_15.covers(AgeBand.AGE_18_PLUS))
    }

    @Test
    fun `escalate steps up one band at a time`() {
        assertEquals(AgeBand.AGE_13_15, AgeBand.AGE_0_12.escalate())
        assertEquals(AgeBand.AGE_16_17, AgeBand.AGE_13_15.escalate())
        assertEquals(AgeBand.AGE_18_PLUS, AgeBand.AGE_16_17.escalate())
    }

    @Test
    fun `escalate at the top band stays at the top`() {
        assertEquals(AgeBand.AGE_18_PLUS, AgeBand.AGE_18_PLUS.escalate())
    }
}
