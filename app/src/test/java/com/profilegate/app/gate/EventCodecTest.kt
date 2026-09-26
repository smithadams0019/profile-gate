package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventCodecTest {

    private val event = GateEvent(
        atMillis = 1_700_000_000_000L,
        titleId = "t6",
        titleName = "Nebula Cadets: Lockdown Special",
        contentBand = AgeBand.AGE_13_15,
        declaredBand = AgeBand.AGE_0_12,
        presence = Presence.CHILD_LIKE,
        decision = GateDecision(GateState.FLAGGED, "matches the declared child band"),
        resolution = GateEvent.Resolution.FLAGGED_REFUSED,
    )

    @Test
    fun `an encoded event decodes back to an equal event`() {
        assertEquals(event, EventCodec.decode(EventCodec.encode(event)))
    }

    @Test
    fun `a line with the wrong number of fields decodes to null, not a crash`() {
        assertNull(EventCodec.decode("not\u0001enough\u0001fields"))
    }

    @Test
    fun `a line with an invalid enum value decodes to null`() {
        val corrupted = EventCodec.encode(event).replaceFirst("AGE_13_15", "NOT_A_BAND")
        assertNull(EventCodec.decode(corrupted))
    }

    @Test
    fun `garbage input decodes to null`() {
        assertNull(EventCodec.decode("complete garbage"))
    }
}
