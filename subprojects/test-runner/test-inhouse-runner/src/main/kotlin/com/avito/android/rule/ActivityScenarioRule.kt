package com.avito.android.rule

import android.app.Activity
import androidx.test.espresso.intent.Intents
import com.avito.android.rule.base.BaseActivityScenarioRule

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
