package com.profilegate.app.gate

import android.content.SharedPreferences

/**
 * Persists the household log across app restarts via [SharedPreferences]. Thin
 * on purpose: the actual line format is [EventCodec], which is Android-free and
 * unit tested on its own.
 */
class EventLogStore(private val prefs: SharedPreferences) {
    fun load(): List<GateEvent> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return raw.split("\n").mapNotNull { EventCodec.decode(it) }
    }

    fun save(events: List<GateEvent>) {
        prefs.edit().putString(KEY, events.joinToString("\n") { EventCodec.encode(it) }).apply()
    }

    companion object {
        private const val KEY = "events"
        const val PREFS_NAME = "profile_gate_log"
    }
}
