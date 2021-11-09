package com.avito.android.ui.test.retry

import androidx.test.espresso.EspressoException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.RetryActivity
import com.avito.android.ui.test.Screen
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RetryTest {

    @get:Rule
    val rule = screenRule<RetryActivity>(launchActivity = true)

    @Test
    fun failWithOriginalError_oneShotActionFailedWithUnexpectedError() {
        // We must preserve an original error for a user
        // It can get lost accidentally due to UITestConfig.waiterAllowedExceptions
        Assert.assertThrows(UnexpectedFatalError::class.java) {
            Screen.retry.button.firstFail(UnexpectedFatalError()).click()
        }
    }

    @Test
    fun success_skipOneTimeUnknownEspressoError() {
        Screen.retry.button.firstFail(UnknownEspressoException()).click()

        Screen.retry.buttonClickIndicator.checks.isDisplayed()
    }

    class UnknownEspressoException : RuntimeException(), EspressoException

    class UnexpectedFatalError : RuntimeException()
}
