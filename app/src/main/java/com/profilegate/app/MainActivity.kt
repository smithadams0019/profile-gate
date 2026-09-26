package com.profilegate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.profilegate.app.age.AgeSignalProvider
import com.profilegate.app.gate.EventLogStore
import com.profilegate.app.ui.ProfileGateApp
import com.profilegate.app.vision.HttpFrameClassifierClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = AppController(
            ageSignalProvider = AgeSignalProvider(contentResolver),
            frameClassifierClient = HttpFrameClassifierClient(assets),
            eventLogStore = EventLogStore(getSharedPreferences(EventLogStore.PREFS_NAME, MODE_PRIVATE)),
        )
        controller.start()

        setContent { ProfileGateApp(controller) }
    }
}
