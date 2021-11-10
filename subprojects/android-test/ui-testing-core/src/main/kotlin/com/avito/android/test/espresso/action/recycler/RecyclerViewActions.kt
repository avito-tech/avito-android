package com.avito.android.test.espresso.action.recycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.util.HumanReadables
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher

// TODO reuse logic with ActionOnItemAtPositionViewAction, ActionOnItemViewAction
private class ViewDoesNotExistInRecyclerCheckHack<VH : RecyclerView.ViewHolder> constructor(
    private val match: RecyclerItemsMatcher.Match<VH>,
    private val viewAction: ViewAction
) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun getDescription(): String = when (match) {
        is RecyclerItemsMatcher.Match.All
        -> "performing ViewAction: ${viewAction.description} on item matching: ${match.matcher}"
        is RecyclerItemsMatcher.Match.AtPosition
        -> "performing ViewAction: ${viewAction.description} on ${match.position}-th item matching: ${match.matcher}"
    }

    override fun perform(uiController: UiController, root: View) {
        val recyclerView = root as RecyclerView
        try {
            uiController.loopMainThreadUntilIdle()

            val matchResult = RecyclerItemsMatcher(recyclerView)
                .match(
                    match
                )
            when (matchResult) {
                is RecyclerItemsMatcher.Result.Found -> throw AssertionError(
                    "We need item doesn't exist, but: ${matchResult.description}"
                )
                is RecyclerItemsMatcher.Result.IndexOutOfBound -> throw AssertionError(
                    matchResult.description
                )
                else -> {
                    // do nothing
                }
            }

            uiController.loopMainThreadUntilIdle()
        } catch (t: Throwable) {
            throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(root)) // TODO replace with recycler view description
                .withCause(t)
                .build()
        }
    }
}

fun <VH : RecyclerView.ViewHolder> itemDoesNotExists(
    itemViewMatcher: Matcher<View>,
    viewAction: ViewAction,
    position: Int? = null
): ViewAction = ViewDoesNotExistInRecyclerCheckHack(
    match = RecyclerItemsMatcher.Match.create(
        position = position,
        matcher = viewHolderMatcher<VH>(itemViewMatcher)
    ),
    viewAction = viewAction
)

fun <VH : RecyclerView.ViewHolder> actionOnItemAtPosition(
    position: Int,
    viewAction: ViewAction
): ViewAction {
    return ActionOnItemAtPositionViewAction<VH>(
        position = position,
        viewAction = viewAction
    )
}

private class ActionOnItemAtPositionViewAction<VH : RecyclerView.ViewHolder>(
    private val position: Int,
    private val viewAction: ViewAction
) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun getDescription(): String =
        "actionOnItemAtPosition performing ViewAction: " + viewAction.description +
            " on item at position: " + position

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        uiController.loopMainThreadUntilIdle()

        @Suppress("UNCHECKED_CAST")
        val viewHolderForPosition = recyclerView.findViewHolderForAdapterPosition(position) as VH?
            ?: throw PerformException.Builder()
                .withActionDescription(this.toString())
                .withViewDescription(HumanReadables.describe(view)) // TODO describe RecyclerView
                .withCause(IllegalStateException("No view holder at position: $position"))
                .build()

        val viewAtPosition = viewHolderForPosition.itemView
        viewAction.perform(uiController, viewAtPosition)

        uiController.loopMainThreadUntilIdle()
    }
}

private class ActionOnItemViewAction<VH : RecyclerView.ViewHolder>(
    private val match: RecyclerItemsMatcher.Match<VH>,
    private val viewAction: ViewAction,
    private val needScroll: Boolean
) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun getDescription(): String = when (match) {
        is RecyclerItemsMatcher.Match.All
        -> "performing ViewAction: ${viewAction.description} on item matching: ${match.matcher}"
        is RecyclerItemsMatcher.Match.AtPosition
        -> "performing ViewAction: ${viewAction.description} on ${match.position}-th item matching: ${match.matcher}"
    }

    override fun perform(uiController: UiController, root: View) {
        val recyclerView = root as RecyclerView
        try {
            uiController.loopMainThreadUntilIdle()

            val matchResult = RecyclerItemsMatcher(recyclerView)
                .match(
                    match
                )
            when (matchResult) {
                is RecyclerItemsMatcher.Result.NoItem,
                is RecyclerItemsMatcher.Result.IndexOutOfBound,
                is RecyclerItemsMatcher.Result.NoItemAtPosition
                -> throw AssertionError(
                    matchResult.description
                )
                is RecyclerItemsMatcher.Result.Found -> {
                    val matchedItem = matchResult.item
                    if (needScroll) {
                        scrollToPosition(matchedItem.position).perform(uiController, root)
                        uiController.loopMainThreadUntilIdle()
                    }
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        position = matchedItem.position,
                        viewAction = viewAction
                    ).perform(uiController, root)
                }
            }
        } catch (t: Throwable) {
            throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(root)) // TODO describe RecyclerView
                .withCause(t)
                .build()
        }
    }
}

fun <VH : RecyclerView.ViewHolder> actionOnHolderItem(
    viewHolderMatcher: TypeSafeMatcher<VH>,
    viewAction: ViewAction,
    position: Int? = null,
    needScroll: Boolean
): ViewAction =
    ActionOnItemViewAction(
        match = RecyclerItemsMatcher.Match.create(
            position = position,
            matcher = viewHolderMatcher
        ),
        viewAction = viewAction,
        needScroll = needScroll
    )

fun <VH : RecyclerView.ViewHolder> actionOnItem(
    itemViewMatcher: Matcher<View>,
    viewAction: ViewAction,
    position: Int? = null,
    needScroll: Boolean
): ViewAction = actionOnHolderItem(
    viewHolderMatcher<VH>(itemViewMatcher),
    viewAction,
    position,
    needScroll
)

class ViewActionOnItemAtPosition<VH : RecyclerView.ViewHolder>(
    private val position: Int,
    private val viewAction: ViewAction
) : ViewAction {

    override fun getConstraints(): Matcher<View> =
        allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun getDescription(): String =
        "performing ViewAction: " + viewAction.description + " on item at position: " + position

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView

        @Suppress("UNCHECKED_CAST")
        val viewHolderForPosition: RecyclerView.ViewHolder? =
            recyclerView.findViewHolderForLayoutPosition(position) as VH?

        val viewAtPosition: View = viewHolderForPosition?.itemView
            ?: throw PerformException.Builder()
                .withActionDescription(this.toString())
                .withViewDescription(HumanReadables.describe(recyclerView)) // TODO Add recyclerview description
                .withCause(IllegalStateException("No view at position: $position"))
                .build()

        viewAction.perform(uiController, viewAtPosition)
    }
}
