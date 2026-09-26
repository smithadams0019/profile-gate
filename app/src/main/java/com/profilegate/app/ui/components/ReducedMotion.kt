package com.profilegate.app.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the viewer has turned animation off at the system level.
 *
 * Fire TV has no "reduce motion" toggle of the kind iOS has; what it does have
 * is `Settings.Global.ANIMATOR_DURATION_SCALE`, set to 0 by Developer Options'
 * "Animator duration scale: off" and by anyone using an accessibility profile
 * that disables animation. the shared Fire TV craft reference's item 48 asks for it to be
 * honoured, and it is the only reduced-motion signal this device exposes.
 *
 * It is read once per composition rather than observed, because it does not
 * change while an app is in the foreground and a `ContentObserver` here would
 * be more moving parts than the setting is worth.
 */
@Composable
fun reducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}
