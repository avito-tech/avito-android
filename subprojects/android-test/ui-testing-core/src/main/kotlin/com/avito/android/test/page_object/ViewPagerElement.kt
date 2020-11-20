package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.espresso.action.ViewPagersFlipAction
import com.avito.android.test.espresso.action.ViewPagersSelectAction
import com.avito.android.test.matcher.ViewPagersSelectMatcher
import com.avito.android.test.matcher.ViewPagersTabsCountMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

open class ViewPagerElement(
    interactionContext: InteractionContext
) : ViewElement(interactionContext),
    ViewPagerActions by ViewPagerActionsImpl(interactionContext) {

    override val checks: ViewPagerChecks = ViewPagerChecksImpl(interactionContext)

    // TODO: Use element()
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    protected inline fun <reified T : PageObjectElement> currentPageElement(matcher: Matcher<View>): T =
        T::class.java.getConstructor(InteractionContext::class.java)
            .newInstance(
                interactionContext.provideChildContext(
                    allOf(
                        // current page is only one completely displayed element inside view pager
                        isCompletelyDisplayed(),
                        matcher
                    )
                )
            )

    /**
     * Hide parent method [PageObject.element]
     */
    protected inline fun <reified T : PageObjectElement> element(@Suppress("UNUSED_PARAMETER") matcher: Matcher<View>): T =
        throw RuntimeException(
            "Use currentPageElement(Matcher<View>) instead for getting page of view pager" +
                "and then use element(Matcher<View>) for getting child"
        )

    /**
     * Hide parent method [PageObject.element]
     */
    protected inline fun <reified T : PageObjectElement> element(): T =
        throw RuntimeException(
            "Use currentPageElement(Matcher<View>) instead for getting page of view pager" +
                "and then use element(Matcher<View>) for getting child"
        )
}

interface ViewPagerActions {
    fun toRight()
    fun toLeft()
    fun select(position: Int)
}

class ViewPagerActionsImpl(private val actionsDriver: ActionsDriver) : ViewPagerActions {

    override fun toRight() {
        actionsDriver.perform(ViewPagersFlipAction(ViewPagersFlipAction.Direction.RIGHT))
    }

    override fun toLeft() {
        actionsDriver.perform(ViewPagersFlipAction(ViewPagersFlipAction.Direction.LEFT))
    }

    override fun select(position: Int) {
        actionsDriver.perform(ViewPagersSelectAction(position))
    }
}

interface ViewPagerChecks : Checks {
    fun withSelectedPosition(position: Int)
    fun withTabsCount(count: Int)
}

class ViewPagerChecksImpl(
    private val driver: ChecksDriver
) : ViewPagerChecks,
    Checks by ChecksImpl(driver) {

    override fun withSelectedPosition(position: Int) {
        driver.check(ViewAssertions.matches(ViewPagersSelectMatcher(position)))
    }

    override fun withTabsCount(count: Int) {
        driver.check(ViewAssertions.matches(ViewPagersTabsCountMatcher(count)))
    }
}
