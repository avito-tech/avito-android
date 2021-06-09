@file:Suppress("unused")
package com.avito.android.rule

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.util.continuousAssertion
import com.avito.android.util.waitForAssertion
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class InHouseScenarioScreenRule<A : Activity>(activityClass: Class<A>) : TestRule {

    protected val androidInstrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val appContext: Context = androidInstrumentation.targetContext.applicationContext
    protected val testContext: Context = androidInstrumentation.context

    private val activityRule = ActivityScenarioRule(activityClass)

    val checks = ChecksLibrary { activityRule.scenario }

    val activityResult: Instrumentation.ActivityResult
        get() = activityRule.scenario.result

    val scenario: ActivityScenario<A>
        get() = activityRule.scenario

    fun launchActivity(startIntent: Intent?): ActivityScenario<A> = activityRule.launchActivity(startIntent)

    class ChecksLibrary<A : Activity>(private val scenarioFunc: () -> ActivityScenario<A>) {

        /**
         * Asserts that activity is in specific state.
         * For more information on lifecycle states, see [androidx.lifecycle.Lifecycle.State]
         */
        fun isInState(state: Lifecycle.State) {
            waitForAssertion { assertThat(scenarioFunc().state).isEqualTo(state) }
        }

        /**
         * Asserts that activity is not in specific state.
         * For more information on lifecycle states, see [androidx.lifecycle.Lifecycle.State]
         */
        fun isNotInState(state: Lifecycle.State) {
            continuousAssertion { assertThat(scenarioFunc().state).isNotEqualTo(state) }
        }

        /**
         * Asserts that activity is in greater or equal state
         * For more information, see [androidx.lifecycle.Lifecycle.State.isAtLeast]
         */
        fun isAtLeastInState(state: Lifecycle.State) {
            waitForAssertion { assertThat(scenarioFunc().state.isAtLeast(state)).isTrue() }
        }

        fun activityResult(expectedResult: Int) {
            val actualResult = scenarioFunc().result.resultCode

            waitForAssertion {
                assertWithMessage("Activity result code mismatch").that(actualResult).isEqualTo(expectedResult)
            }
        }
    }

    override fun apply(base: Statement, description: Description): Statement = activityRule.apply(base, description)
}
