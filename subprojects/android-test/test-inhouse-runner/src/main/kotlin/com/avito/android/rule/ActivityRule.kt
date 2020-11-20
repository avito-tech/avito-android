package com.avito.android.rule

import android.app.Activity
import androidx.test.espresso.intent.Intents
import androidx.test.rule.ActivityTestRule
import com.avito.android.test.Intents.stubEverything

internal class ActivityRule<T : Activity>(aClass: Class<T>) : ActivityTestRule<T>(
    aClass,
    true,
    false
) {

    override fun afterActivityLaunched() {
        Intents.init()
        stubEverything()
        super.afterActivityLaunched()
    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()
        Intents.release()
    }
}
