package com.avito.android.test

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Root
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.interceptor.AssertionInterceptor
import org.hamcrest.Matcher
import org.hamcrest.Matchers

open class SimpleInteractionContext(
    private val matcher: Matcher<View>,
    private val rootMatcher: Matcher<Root>? = null,
    private val precondition: () -> Unit = {}
) : InteractionContext {

    private var inPrecondition = false

    private val interaction: ViewInteraction
        get() {
            val interaction = Espresso.onView(matcher)

            return if (rootMatcher == null) {
                interaction
            } else {
                interaction.inRoot(rootMatcher)
            }
        }

    override fun perform(vararg actions: ViewAction) {
        runPrecondition()

        interaction.waitToPerform(
            actions.map { action ->
                ActionInterceptor.Proxy(
                    action,
                    UITestConfig.actionInterceptors
                )
            }
        )
    }

    override fun check(assertion: ViewAssertion) {
        runPrecondition()

        interaction.waitForCheck(
            AssertionInterceptor.Proxy(assertion, UITestConfig.assertionInterceptors)
        )
    }

    override fun provideChildContext(matcher: Matcher<View>): InteractionContext =
        SimpleInteractionContext(
            matcher = Matchers.allOf(
                ViewMatchers.isDescendantOfA(this.matcher),
                matcher
            ),
            rootMatcher = rootMatcher,
            precondition = precondition
        )

    private fun runPrecondition() {
        if (!inPrecondition) {
            inPrecondition = true
            precondition()
            inPrecondition = false
        }
    }
}
