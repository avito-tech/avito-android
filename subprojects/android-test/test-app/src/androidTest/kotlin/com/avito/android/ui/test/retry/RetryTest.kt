package com.avito.android.ui.test.retry

import com.avito.android.ui.RetryActivity
import com.avito.android.ui.UnexpectedFatalError
import com.avito.android.ui.test.Screen
import com.avito.android.ui.test.screenRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.avito.util.instanceOf

class RetryTest {

    @get:Rule
    val rule = screenRule<RetryActivity>(launchActivity = true)

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun hasOriginalError_oneShotActionFailedWithUnknownCause() {
        // We must preserve an original error for a user
        // It can get lost accidentally due to UITestConfig.waiterAllowedExceptions
        exception.expectCause(instanceOf<UnexpectedFatalError>())

        Screen.retry.button.click()

        Screen.retry.buttonClickIndicator.checks.isDisplayed()
    }
}
