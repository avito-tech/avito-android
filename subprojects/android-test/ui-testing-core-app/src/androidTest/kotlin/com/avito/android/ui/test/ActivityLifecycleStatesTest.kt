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
    fun finishedActivity_isInDestroyedState_succeed() {
        activityRule.scenario.onActivity { it.finish() }
        activityRule.checks.isInState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun finishedActivity_isNotInDestroyedState_throwsAssertionError() {
        activityRule.scenario.onActivity { it.finish() }
        assertThrows<AssertionError> { activityRule.checks.isNotInState(Lifecycle.State.DESTROYED) }
    }

    @Test
    fun launchedActivity_isNotInDestroyedState_succeed() {
        activityRule.checks.isNotInState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun launchedActivity_isInDestroyedState_throwsAssertionError() {
        assertThrows<AssertionError> { activityRule.checks.isInState(Lifecycle.State.DESTROYED) }
    }

    @Test
    fun launchedActivity_isAtLeastInCreatedState_succeed() {
        activityRule.checks.isAtLeastInState(Lifecycle.State.CREATED)
    }

    @Test
    fun launchedActivity_isInResumedState_succeed() {
        activityRule.checks.isInState(Lifecycle.State.RESUMED)
    }
}
