package com.profilegate.app.vision

import java.net.HttpURLConnection
import java.net.URL

/**
 * A plain reachability probe against the frame classifier service's `/health`
 * endpoint, separate from [FrameClassifierClient] so that interface can stay a
 * single-method `fun interface` (and so tests can keep stubbing it with a
 * lambda). Used only by the settings screen's "Test connection" affordance --
 * never in the gate decision path itself.
 */
object HealthCheck {
    fun isReachable(baseUrl: String, timeoutMillis: Int = 2_000): Boolean {
        return try {
            val connection = URL("$baseUrl/health").openConnection() as HttpURLConnection
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.requestMethod = "GET"
            val reachable = connection.responseCode == 200
            connection.disconnect()
            reachable
        } catch (e: Exception) {
            false
        }
    }
}
