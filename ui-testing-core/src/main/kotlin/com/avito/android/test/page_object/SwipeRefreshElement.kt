package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.SwipeDirections
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.matcher.IsRefreshingMatcher
import org.hamcrest.Matcher
import org.hamcrest.core.Is.`is`

class SwipeRefreshElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext) {

    // TODO: remove this constructor and use element fabric method to create an instance
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    override val checks: SwipeRefreshChecks = SwipeRefreshChecksImpl(interactionContext)
    override val actions: SwipeRefreshActions = SwipeRefreshActionsImpl(interactionContext)
}

interface SwipeRefreshActions : Actions {

    fun pullToRefresh()
}

class SwipeRefreshActionsImpl(
    private val driver: ActionsDriver
) : SwipeRefreshActions,
    Actions by ActionsImpl(driver) {

    override fun pullToRefresh() {
        driver.perform(SwipeRefreshTolerantAction())
    }
}

interface SwipeRefreshChecks : Checks {

    fun isRefreshing()
    fun isNotRefreshing()
}

class SwipeRefreshChecksImpl(
    private val driver: ChecksDriver
) : SwipeRefreshChecks,
    Checks by ChecksImpl(driver) {

    override fun isRefreshing() {
        driver.check(matches(IsRefreshingMatcher(`is`(true))))
    }

    override fun isNotRefreshing() {
        driver.check(matches(IsRefreshingMatcher(`is`(false))))
    }
}

/**
 * WISDOM
 * sometimes default GeneralSwipeAction constraint fails, need to override it.
 * see https://stackoverflow.com/questions/33505953/espresso-how-to-test-swiperefreshlayout
 * */
class SwipeRefreshTolerantAction : ViewAction {

    val action = EspressoActions.swipe(SwipeDirections.TOP_TO_BOTTOM)

    override fun getDescription() =
        "SwipeRefreshLayout performing ViewAction: ${action.description} " +
                "with tolerance $VIEW_DISPLAY_PERCENTAGE"

    override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE)

    override fun perform(uiController: UiController?, view: View?) {
        action.perform(uiController, view)
    }
}

private const val VIEW_DISPLAY_PERCENTAGE = 85
