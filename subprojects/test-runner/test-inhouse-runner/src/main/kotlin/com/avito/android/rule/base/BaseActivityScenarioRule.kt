package com.avito.android.rule.base

import android.app.Activity
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.rule.SimpleRule

open class BaseActivityScenarioRule<A : Activity>(
    private val activityClass: Class<A>,
    private val initialTouchMode: Boolean,
    private val launchActivity: Boolean,
) : SimpleRule() {

    private var _scenario: ActivityScenario<A>? = null
    val scenario: ActivityScenario<A> get() = checkNotNull(_scenario) {
        buildString {
            append("Activity $activityClass has not been launched. ")
            append("Start activity using `launchActivity(...)` function first ")
            append("or pass `launchActivity = true` parameter to constructor.")
        }
    }

    override fun before() {
        super.before()
        if (launchActivity) {
            launchActivity(null)
        }
    }

    override fun after() {
        super.after()
        val scenario = checkNotNull(_scenario) {
            buildString {
                append("Activity $activityClass has not been launched during the test. ")
                append("Make sure that there are no unused ScreenRules declared in the test but never used.")
            }
        }
        scenario.close()
        afterActivityFinished()
    }

    fun launchActivity(intent: Intent? = null): ActivityScenario<A> {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(initialTouchMode)
        _scenario = when (intent) {
            null -> ActivityScenario.launch(activityClass)
            else -> ActivityScenario.launch(
                intent.setClass(InstrumentationRegistry.getInstrumentation().targetContext, activityClass)
            )
        }
        afterActivityLaunched()
        return scenario
    }

    @CallSuper
    protected open fun afterActivityLaunched() {
        // empty
    }

    @CallSuper
    protected open fun afterActivityFinished() {
        // empty
    }
}
