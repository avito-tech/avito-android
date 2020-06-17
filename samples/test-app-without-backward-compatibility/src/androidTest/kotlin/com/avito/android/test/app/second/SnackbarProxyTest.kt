package com.avito.android.test.app.second

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.test.Screen
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class SnackbarProxyTest {

    @get:Rule
    val rule = screenRule<SnackbarProxyTestActivity>()

    @get:Rule
    val snackbarRule = SnackbarRule()

    val snackbarChecks = snackbarRule.checks

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun check_snackbar_is_shown() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarChecks.isShownWithExactlyText("snackbar number 1")
    }

    @Test
    fun check_snackbar_is_shown_last() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarChecks.isShownLastWithExactlyText("snackbar number 2")
    }

    @Test
    fun check_snackbar_is_shown_N_times() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarChecks.isShownWithTextMatches(Matchers.containsString("snackbar number"), 2)
    }

    @Test
    fun check_delayed_snackbar_is_shown() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()

        snackbarChecks.isShownWithExactlyText("snackbar number 1")
    }

    @Test
    fun check_delayed_snackbar_is_shown_last() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()

        snackbarChecks.isShownLastWithExactlyText("snackbar number 3")
    }

    @Test
    fun check_snackbar_is_shown_after_delay() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()

        simulateDelay()

        snackbarChecks.isShownWithExactlyText("snackbar number 1")
    }

    @Test
    fun check_snackbar_is_shown_last_after_delay() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        simulateDelay()

        snackbarChecks.isShownLastWithExactlyText("snackbar number 2")
    }

    @Test
    fun check_no_snackbar_after_clear_rule() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()
        val text = "snackbar number 1"
        snackbarChecks.isShownLastWithExactlyText(text)
        snackbarRule.clear()

        exception.expect(java.lang.AssertionError::class.java)
        exception.expectMessage("There weren't shown any snackbar")
        snackbarChecks.isShownLastWithExactlyText(text)
    }

    /**
     * We are against using Thread.sleep in tests
     * In this case we use sleep for testing our framework behaviour
     */
    private fun simulateDelay() {
        Thread.sleep(1000L)
    }
}
