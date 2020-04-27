package com.avito.android.test

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.isDoesntExistAssertion
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.espresso.action.GroupedViewAction
import com.avito.android.test.espresso.action.recycler.actionOnItem
import com.avito.android.test.espresso.action.recycler.itemDoesNotExists
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.interceptor.AssertionInterceptor
import com.forkingcode.espresso.contrib.DescendantViewActions.checkDescendantViewAction
import com.forkingcode.espresso.contrib.DescendantViewActions.performDescendantAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

interface InteractionContext : ActionsDriver, ChecksDriver {

    fun provideChildContext(matcher: Matcher<View>): InteractionContext
}

class SimpleInteractionContext(
    private val matcher: Matcher<View>,
    private val precondition: () -> Unit = {}
) : InteractionContext {

    private var inPrecondition = false

    private val interaction: ViewInteraction
        get() = Espresso.onView(matcher)

    override fun perform(vararg actions: ViewAction) {
        runPrecondition()

        interaction.waitToPerform(actions.map { action ->
            ActionInterceptor.Proxy(
                action,
                UITestConfig.actionInterceptors
            )
        })
    }

    override fun check(assertion: ViewAssertion) {
        runPrecondition()

        interaction.waitForCheck(
            AssertionInterceptor.Proxy(assertion, UITestConfig.assertionInterceptors)
        )
    }

    override fun provideChildContext(matcher: Matcher<View>): InteractionContext =
        SimpleInteractionContext(allOf(isDescendantOfA(this.matcher), matcher), precondition)

    private fun runPrecondition() {
        if (!inPrecondition) {
            inPrecondition = true
            precondition()
            inPrecondition = false
        }
    }
}

class RecyclerViewInteractionContext(
    private val interactionContext: InteractionContext,
    private val cellMatcher: Matcher<View>,
    private val childMatcher: Matcher<View>,
    private val position: Int?,
    private val needScroll: Boolean
) : InteractionContext {

    override fun perform(vararg actions: ViewAction) {
        val groupedAction = GroupedViewAction(actions.toList())

        val actionOnItem = actionOnItem<RecyclerView.ViewHolder>(
            itemViewMatcher = cellMatcher,
            viewAction = performDescendantAction(childMatcher, groupedAction),
            position = position,
            needScroll = needScroll
        )

        ActionInterceptor.Proxy(
            source = actionOnItem,
            interceptors = UITestConfig.actionInterceptors
        )

        interactionContext.perform(actionOnItem)
    }

    override fun check(assertion: ViewAssertion) {
        val intercepted = AssertionInterceptor.Proxy(assertion, UITestConfig.assertionInterceptors)

        if (assertion.isDoesntExistAssertion()) {
            interactionContext.perform(
                itemDoesNotExists<RecyclerView.ViewHolder>(
                    itemViewMatcher = cellMatcher,
                    viewAction = checkDescendantViewAction(childMatcher, intercepted),
                    position = position
                )
            )
        } else {
            interactionContext.perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    itemViewMatcher = cellMatcher,
                    viewAction = checkDescendantViewAction(childMatcher, intercepted),
                    position = position,
                    needScroll = needScroll
                )
            )
        }
    }

    override fun provideChildContext(matcher: Matcher<View>): InteractionContext =
        RecyclerViewInteractionContext(
            interactionContext = interactionContext,
            cellMatcher = cellMatcher,
            childMatcher = allOf(isDescendantOfA(childMatcher), matcher),
            position = position,
            needScroll = needScroll
        )
}
