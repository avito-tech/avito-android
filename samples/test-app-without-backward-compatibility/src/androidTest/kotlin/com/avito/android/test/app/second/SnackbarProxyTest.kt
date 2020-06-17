package com.avito.android.test.app.second

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.test.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class SnackbarProxyTest {

    @get:Rule
    val rule = screenRule<SnackbarProxyTestActivity>()

    @get:Rule
    val snackbarRule = SnackbarProxyRule()

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun check_snackbar_is_shown() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarRule.isShown("snackbar number 1")
    }

    @Test
    fun check_snackbar_is_shown_last() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarRule.isShownLast("snackbar number 2")
    }

    @Test
    fun check_snackbar_is_shown_N_times() {
        rule.launchActivity(null)

        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        snackbarRule.isShownTimes("snackbar number 1", 1)
    }

    @Test
    fun check_delayed_snackbar_is_shown() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()

        snackbarRule.isShown("snackbar number 1")
    }

    @Test
    fun check_delayed_snackbar_is_shown_last() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()
        Screen.snackbarProxyScreen.clickShowSnackbarDelayed()

        snackbarRule.isShownLast("snackbar number 3")
    }

    @Test
    fun check_snackbar_is_shown_after_delay() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()

        Thread.sleep(1000L)

        snackbarRule.isShown("snackbar number 1")
    }

    @Test
    fun check_snackbar_is_shown_last_after_delay() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()
        Screen.snackbarProxyScreen.clickShowSnackbar()

        Thread.sleep(1000L)

        snackbarRule.isShownLast("snackbar number 2")
    }

    @Test
    fun check_no_snackbar_after_clear_rule() {
        rule.launchActivity(null)
        Screen.snackbarProxyScreen.clickShowSnackbar()
        val text = "snackbar number 1"
        snackbarRule.isShownLast(text)
        snackbarRule.clear()

        exception.expect(java.lang.AssertionError::class.java)
        exception.expectMessage("Snackbar with text=\"$text\" wasn't shown last")
        snackbarRule.isShownLast(text)
    }
}
