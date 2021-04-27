package com.avito.android.rule

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import java.lang.ref.WeakReference

/**
 * This rule provides functional testing of a single [Activity] similar to deprecated
 * [androidx.test.rule.ActivityTestRule].
 * @param activityClass The Activity class under test
 * @param launchActivity true if the Activity should be launched before each test
 */
open class ActivityScenarioRule<A : Activity>(
    private val activityClass: Class<A>,
    private val launchActivity: Boolean
) : ExternalResource() {

    val activity: A
        get() = activityRef?.get() ?: throwUninitializedException()
    val activityResult: Instrumentation.ActivityResult
        get() = scenario?.result ?: throwUninitializedException()

    private var scenario: ActivityScenario<A>? = null
    private var activityRef: WeakReference<A>? = null

    override fun before() {
        super.before()
        if (launchActivity) {
            scenario = ActivityScenario.launch(activityClass)
            afterActivityLaunched()
        }
    }

    override fun after() {
        super.after()
        scenario?.close()
        activityRef = null
        afterActivityFinished()
    }

    fun launchActivity(intent: Intent?): A {
        scenario = when (intent) {
            null -> ActivityScenario.launch(activityClass)
            else -> ActivityScenario.launch(
                intent.setClass(InstrumentationRegistry.getInstrumentation().targetContext, activityClass)
            )
        }
        afterActivityLaunched()
        return activityRef?.get() ?: throwUninitializedException()
    }

    fun runOnUiThread(runnable: Runnable) {
        scenario?.onActivity {
            runnable.run()
        } ?: error("Activity is not launched")
    }

    private fun throwUninitializedException(): Nothing = error("Activity is not initialized")

    @CallSuper
    open fun afterActivityLaunched() {
        scenario?.onActivity { activityRef = WeakReference(it) }
    }

    @CallSuper
    open fun afterActivityFinished() { }
}
