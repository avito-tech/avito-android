package com.avito.android.ui.test

import androidx.lifecycle.Lifecycle
import com.avito.android.test.app.core.inHouseScreenRule
import com.avito.android.ui.EmptyActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class ActivityLifecycleStatesTest {

    @get:Rule
    val activityRule = inHouseScreenRule<EmptyActivity>()

    @Before
    fun before() {
        activityRule.launchActivity(null)
    }

    @Test
    fun isDestroyed_succeed_whenActivityFinished() {
        activityRule.scenario.onActivity { it.finish() }
        activityRule.checks.isInState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun isDestroyed_throwsAssertionError_whenActivityFinished() {
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
        assertThrows<AssertionError> { activityRule.checks.isInState(Lifecycle.State.DESTROYED) }
    }

    @Test
    fun stateIsGreaterThanCreated_succeed_whenActivityLaunched() {
        activityRule.checks.isAtLeastInState(Lifecycle.State.CREATED)
    }

    @Test
    fun isResumed_succeed_whenActivityLaunched() {
        activityRule.checks.isInState(Lifecycle.State.RESUMED)
    }
}
