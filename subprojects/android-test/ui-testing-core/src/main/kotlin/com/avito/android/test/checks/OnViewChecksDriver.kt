package com.avito.android.test.checks

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.AssertionInterceptor
import com.avito.android.test.waitForCheck
import org.hamcrest.Matcher

@Deprecated("todo use InteractionContext")
class OnViewChecksDriver(private val matcher: Matcher<View>) : ChecksDriver {

    private val interaction: ViewInteraction
        get() = Espresso.onView(matcher)

    override fun check(assertion: ViewAssertion) {
        val interceptedAssertion =
            AssertionInterceptor.Proxy(assertion, UITestConfig.assertionInterceptors)

        interaction.waitForCheck(interceptedAssertion)
    }
}
