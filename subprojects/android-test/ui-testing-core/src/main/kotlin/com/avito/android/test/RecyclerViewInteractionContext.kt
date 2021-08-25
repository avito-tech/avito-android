package com.avito.android.test

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.isDoesntExistAssertion
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.espresso.action.GroupedViewAction
import com.avito.android.test.espresso.action.recycler.actionOnItem
import com.avito.android.test.espresso.action.recycler.itemDoesNotExists
import com.avito.android.test.interceptor.AssertionInterceptor
import com.forkingcode.espresso.contrib.DescendantViewActions
import org.hamcrest.Matcher
import org.hamcrest.Matchers

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
            viewAction = DescendantViewActions.performDescendantAction(childMatcher, groupedAction),
            position = position,
            needScroll = needScroll
        )

        interactionContext.perform(actionOnItem)
    }

    override fun check(assertion: ViewAssertion) {
        val intercepted = AssertionInterceptor.Proxy(assertion, UITestConfig.assertionInterceptors)

        if (assertion.isDoesntExistAssertion()) {
            interactionContext.perform(
                itemDoesNotExists<RecyclerView.ViewHolder>(
                    itemViewMatcher = cellMatcher,
                    viewAction = DescendantViewActions.checkDescendantViewAction(childMatcher, intercepted),
                    position = position
                )
            )
        } else {
            interactionContext.perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    itemViewMatcher = cellMatcher,
                    viewAction = DescendantViewActions.checkDescendantViewAction(childMatcher, intercepted),
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
            childMatcher = Matchers.allOf(ViewMatchers.isDescendantOfA(childMatcher), matcher),
            position = position,
            needScroll = needScroll
        )
}
