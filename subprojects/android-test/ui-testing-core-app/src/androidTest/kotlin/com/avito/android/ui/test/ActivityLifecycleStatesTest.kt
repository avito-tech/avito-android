package com.avito.android.ui.test

import androidx.lifecycle.Lifecycle
import com.avito.android.test.app.core.inHouseScreenRule
import com.avito.android.ui.EmptyActivity
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class ActivityLifecycleStatesTest {

    @get:Rule
    val activityRule = inHouseScreenRule<EmptyActivity>()

    @Test
    fun isDestroyed_succeed_whenActivityFinished() {
        activityRule.launchActivity(null)
        activityRule.scenario.onActivity { it.finish() }
        activityRule.checks.isInState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun isDestroyed_throwsAssertionError_whenActivityFinished() {
        activityRule.launchActivity(null)
        activityRule.scenario.onActivity { it.finish() }
        assertThrows<AssertionError> { activityRule.checks.isNotInState(Lifecycle.State.DESTROYED) }
    }

    @Test
    fun isNotDestroyed_succeed_whenActivityLaunched() {
        activityRule.launchActivity(null)
        activityRule.checks.isNotInState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun isDestroyed_throwsAssertionError_whenActivityLaunched() {
        activityRule.launchActivity(null)
        assertThrows<AssertionError> { activityRule.checks.isInState(Lifecycle.State.DESTROYED) }
    }

    @Test
    fun stateIsGreaterThanCreated_succeed_whenActivityLaunched() {
        activityRule.launchActivity(null)
        activityRule.checks.isAtLeastInState(Lifecycle.State.CREATED)
    }

    @Test
    fun isResumed_succeed_whenActivityLaunched() {
        activityRule.launchActivity(null)
        activityRule.checks.isInState(Lifecycle.State.RESUMED)
    }

    @Test
    fun stateChecks_throwIllegalStateException_whenActivityIsNotLaunched() {
        assertThrows<IllegalStateException> { activityRule.checks.isInState(Lifecycle.State.DESTROYED) }
        assertThrows<IllegalStateException> { activityRule.checks.isInState(Lifecycle.State.INITIALIZED) }
        activityRule.launchActivity(null) // or test will fail
    }
}
