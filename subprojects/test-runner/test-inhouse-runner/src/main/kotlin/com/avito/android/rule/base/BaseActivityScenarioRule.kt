package com.avito.android.rule.base

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner

public abstract class BaseActivityScenarioRule<A : Activity>(
    private val activityClass: Class<A>,
    private val initialTouchMode: Boolean,
    private val launchActivity: Boolean,
) : SimpleRule() {

    private val shouldCloseScenario: Boolean =
        (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner)
            .shouldCloseScenarioInRule

    private var _scenario: ActivityScenario<A>? = null

    public val scenario: ActivityScenario<A>
        get() = checkNotNull(_scenario) {
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
        checkNotNull(_scenario) {
            buildString {
                append("Activity $activityClass has not been launched during the test. ")
                append("Make sure that there are no unused ScreenRules declared in the test but never used.")
            }
        }
        if (shouldCloseScenario) {
            /**
             * Closing it's time consuming some tests waiting for 45 sec
             * And we don't know do we really have to close it
             */
            scenario.close()
        }
        afterActivityFinished()
    }

    public fun launchActivity(intent: Intent? = null): ActivityScenario<A> {
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

    public fun launchActivityForResult(intent: Intent? = null): ActivityScenario<A> {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(initialTouchMode)
        _scenario = when (intent) {
            null -> ActivityScenario.launchActivityForResult(activityClass)
            else -> ActivityScenario.launchActivityForResult(
                intent.setClass(InstrumentationRegistry.getInstrumentation().targetContext, activityClass)
            )
        }
        afterActivityLaunched()
        return scenario
    }

    protected abstract fun afterActivityLaunched()

    protected abstract fun afterActivityFinished()
}
