package com.avito.android.test.compose

import android.view.View
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import com.avito.android.test.InteractionContext
import com.avito.android.test.UITestConfig
import com.avito.android.test.compose.action.ComposeAction
import com.avito.android.test.compose.assertion.ComposeAssertion
import com.avito.android.test.compose.filter.ComposeFilter
import com.avito.android.test.compose.interceptor.ComposeActionInterceptor
import com.avito.android.test.compose.interceptor.ComposeAssertionInterceptor
import org.hamcrest.Matcher

public class ComposeInteractionContext(
    private val parentViewInteractionContext: InteractionContext,
    public val provider: SemanticsNodeInteractionsProvider,
    public val filter: ComposeFilter
) : InteractionContext {

    private val node: SemanticsNodeInteraction
        get() = provider.onAllNodes(filter.matcher, filter.useUnmergedTree)[filter.position]

    override fun check(assertion: ViewAssertion) {
        throw unsupportedOperation("check")
    }

    override fun perform(vararg actions: ViewAction) {
        throw unsupportedOperation("perform")
    }

    private fun unsupportedOperation(name: String): Throwable {
        return ComposeUnsupportedException(
            "`$name` view is not supported for ComposeInteractionContext." +
                "Check which InteractionContext is used when creating the PageObject."
        )
    }

    override fun provideChildContext(matcher: Matcher<View>): InteractionContext {
        return parentViewInteractionContext.provideChildContext(matcher)
    }

    public fun provideChildContext(filter: ComposeFilter): ComposeInteractionContext {
        return ComposeInteractionContext(
            parentViewInteractionContext = parentViewInteractionContext,
            provider = provider,
            filter = filter
        )
    }

    public fun onNode(): SemanticsNodeInteraction = node

    public fun check(assertion: ComposeAssertion) {
        ComposeAssertionInterceptor.Proxy(
            source = assertion,
            interceptors = UITestConfig.assertionInterceptors
        ).check(node)
    }

    public fun perform(action: ComposeAction) {
        ComposeActionInterceptor.Proxy(
            source = action,
            interceptors = UITestConfig.actionInterceptors
        ).perform(node)
    }
}
