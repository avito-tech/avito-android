package com.avito.android.rule

import android.app.Activity
import androidx.test.espresso.intent.Intents
import com.avito.android.test.Intents.stubEverything

/**
 * TODO: Remove this rule after MBS-9536
 */
internal class ActivityRule<T : Activity>(aClass: Class<T>) :
    @Suppress("DEPRECATION") androidx.test.rule.ActivityTestRule<T>(
        aClass,
        true, // initialTouchMode
        false // launchActivity
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
