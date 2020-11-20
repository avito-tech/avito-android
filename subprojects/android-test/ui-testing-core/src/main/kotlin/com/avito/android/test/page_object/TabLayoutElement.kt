package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.util.HumanReadables
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.matcher.TabLayoutSelectMatcher
import com.avito.android.test.matcher.TabLayoutTabsCountMatcher
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher

class TabLayoutElement(
    interactionContext: InteractionContext
) :
    ViewElement(interactionContext),
    TabLayoutActions by TabLayoutActionsImpl(interactionContext) {

    override val checks: TabLayoutChecks = TabLayoutChecksImpl(interactionContext)
}

interface TabLayoutActions {
    fun select(position: Int)
}

class TabLayoutActionsImpl(private val actionsDriver: ActionsDriver) : TabLayoutActions {
    override fun select(position: Int) {
        actionsDriver.perform(TabLayoutSelectAction(position))
    }
}

interface TabLayoutChecks : Checks {

    fun withSelectedPosition(position: Int)

    fun withTabsCount(count: Int)
}

class TabLayoutChecksImpl(private val driver: ChecksDriver) : TabLayoutChecks,
    Checks by ChecksImpl(driver) {

    override fun withSelectedPosition(position: Int) {
        driver.check(matches(TabLayoutSelectMatcher(position)))
    }

    override fun withTabsCount(count: Int) {
        driver.check(matches(TabLayoutTabsCountMatcher(count)))
    }
}

private class TabLayoutSelectAction(private val tabIndex: Int) : ViewAction {

    override fun getDescription() = "selecting TabLayout"

    override fun getConstraints(): Matcher<View> =
        isAssignableFrom(TabLayout::class.java)

    override fun perform(uiController: UiController, view: View) {
        val tabAtIndex = (view as TabLayout).getTabAt(tabIndex)
            ?: throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(Throwable("No tab at index $tabIndex"))
                .build()

        tabAtIndex.select()
        uiController.loopMainThreadUntilIdle()
    }
}
