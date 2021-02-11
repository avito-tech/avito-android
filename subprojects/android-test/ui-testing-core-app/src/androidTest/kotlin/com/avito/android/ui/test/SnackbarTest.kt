package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.SnackbarActivity
import org.junit.Rule
import org.junit.Test

class SnackbarTest {

    @get:Rule
    val rule = screenRule<SnackbarActivity>()

    @Test
    fun snackbar__isDisplayed_emptyMatcher() {
        rule.launchActivity(null)

        Screen.snackbarScreen.snackbar.checks.isDisplayed()
        Screen.snackbarScreen.snackbar.message.checks.isDisplayed()
    }

    @Test
    fun snackbar__isDisplayed_textMatcher() {
        rule.launchActivity(null)

        with(Screen.snackbarScreen.snackbar("Message")) {
            checks.isDisplayed()
            message.checks.isDisplayed()
        }
    }
}
