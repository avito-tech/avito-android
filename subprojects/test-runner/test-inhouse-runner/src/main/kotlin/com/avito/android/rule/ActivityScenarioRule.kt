package com.avito.android.rule

import android.app.Activity
import androidx.test.espresso.intent.Intents
import com.avito.android.rule.base.BaseActivityScenarioRule

/**
 * Initializes Intents and begins recording intents. Must be called prior to triggering any
 * actions that send out intents which need to be verified or stubbed. This is similar to
 * MockitoAnnotations.initMocks.
 */
class ActivityScenarioRule<T : Activity>(activityClass: Class<T>) : BaseActivityScenarioRule<T>(
    activityClass = activityClass,
    initialTouchMode = true,
    launchActivity = false,
) {

    override fun afterActivityLaunched() {
        Intents.init()
        com.avito.android.test.Intents.stubEverything()
        super.afterActivityLaunched()
    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()
        Intents.release()
    }
}