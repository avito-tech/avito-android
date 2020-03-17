package com.avito.android.ui.test.retry

import androidx.test.espresso.EspressoException
import com.avito.android.ui.RetryActivity
import com.avito.android.ui.test.Screen
import com.avito.android.ui.test.screenRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.avito.util.instanceOf
import java.lang.RuntimeException

class RetryTest {

    @get:Rule
    val rule = screenRule<RetryActivity>(launchActivity = true)

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun failWithOriginalError_oneShotActionFailedWithUnexpectedError() {
        // We must preserve an original error for a user
        // It can get lost accidentally due to UITestConfig.waiterAllowedExceptions
        exception.expectCause(instanceOf<UnexpectedFatalError>())

        Screen.retry.button.firstFail(UnexpectedFatalError()).click()

        Screen.retry.buttonClickIndicator.checks.isDisplayed()
    }

    @Test
    fun success_skipOneTimeUnknownEspressoError() {
        Screen.retry.button.firstFail(UnknownEspressoException()).click()

        Screen.retry.buttonClickIndicator.checks.isDisplayed()
    }

    class UnknownEspressoException : RuntimeException(), EspressoException

    class UnexpectedFatalError : RuntimeException()

}
