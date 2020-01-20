package com.avito.android.test.page_object

import android.view.View
import android.widget.ImageButton
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.avito.android.test.Device
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.UITestConfig
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.espresso.action.ToolbarReadMenuItemsAction
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.matcher.ToolbarSubTitleResMatcher
import com.avito.android.test.matcher.ToolbarSubtitleMatcher
import com.avito.android.test.matcher.ToolbarTitleMatcher
import com.avito.android.test.matcher.ToolbarTitleResMatcher
import com.avito.android.test.waitFor
import com.avito.android.test.waitToPerform
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher

open class ToolbarElement(interactionContext: InteractionContext) :
    ViewElement(interactionContext) {

    constructor() : this(SimpleInteractionContext(isAssignableFrom(Toolbar::class.java))) // TODO: migrate to HandleParentContext
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    override val checks: ToolbarElementChecks = ToolbarElementChecksImpl(interactionContext)

    /**
     * From espresso
     *
     * Ideally, this should be only allOf(isDisplayed(), withContentDescription("More options"))
     * But the ActionBarActivity compat lib is missing a content description for this element, so
     * we add the class name matcher as another option to find the view.
     */
    private val overflowButtonMatcher = anyOf<View>(
        allOf<View>(isDisplayed(), withContentDescription("More options")),
        allOf<View>(isDisplayed(), withClassName(endsWith("OverflowMenuButton")))
    )

    val upButton = ImageViewElement(
        interactionContext.provideChildContext(
            allOf(
                isAssignableFrom(ImageButton::class.java),
                withId(View.NO_ID)
            )
        )
    )

    val overflowMenuButton = ViewElement(overflowButtonMatcher)

    protected fun overflowMenuItem(titleMatcher: Matcher<String>) =
        MenuItem(isAssignableFrom(Toolbar::class.java), titleMatcher, overflowMenuButton)

    protected fun overflowMenuItem(title: String) = overflowMenuItem(equalTo(title))

    protected fun actionMenuItem(titleMatcher: Matcher<String>) =
        MenuItem(isAssignableFrom(Toolbar::class.java), titleMatcher, overflowMenuButton)

    protected fun actionMenuItem(title: String) = actionMenuItem(equalTo(title))

    class MenuItem(
        private val toolbarMatcher: Matcher<View>,
        private val titleMatcher: Matcher<String>,
        private var overflowMenuButton: PageObjectElement?
    ) : ViewElement(RestrictedDirectAccessMatcher()) {

        override val actions: Actions
            get() = ActionsImpl(
                OverflowMenuDriver(
                    toolbarMatcher,
                    titleMatcher,
                    overflowMenuButton
                )
            )

        override val checks
            get() = OverflowMenuChecksImpl(
                toolbarMatcher, titleMatcher,
                ChecksImpl(OverflowMenuDriver(toolbarMatcher, titleMatcher, overflowMenuButton))
            )

        fun withDisabledAutoOpenOverflow(): MenuItem {
            overflowMenuButton = null
            return this
        }
    }

    /**
     * In some cases, there is no need to expand overflow menu,
     * if element could be found with reflection.
     * */
    class OverflowMenuChecksImpl(
        private val toolbarMatcher: Matcher<View>,
        private val titleMatcher: Matcher<String>,
        private val checks: Checks
    ) : Checks by checks {
        private val foundHidden
            get() = ToolbarReadMenuItemsAction().apply {
                Espresso.onView(toolbarMatcher).perform(this)
            }.hasHiddenItem(titleMatcher)

        /** Overflow menu element exists, no matter visible or hidden */
        override fun exists() {
            if (!foundHidden) {
                checks.exists()
            }
        }

        /** Overflow menu element is not visible until expanded */
        fun existsAsHidden() {
            assertThat("Overflow menu element $titleMatcher seems to be hidden", foundHidden)
        }
    }

    private class RestrictedDirectAccessMatcher : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText(
                "Use element.actions.<action> syntax. " +
                    "Direct access restricted for MenuItems"
            )
        }

        override fun matchesSafely(item: View): Boolean = false
    }

    private class OverflowMenuDriver(
        toolbarMatcher: Matcher<View>,
        private val titleMatcher: Matcher<String>,
        private val overflowMenuButton: PageObjectElement?
    ) : ActionsDriver, ChecksDriver {

        private val actionInteraction = Espresso.onView(
            allOf(
                isDescendantOfA(isAssignableFrom(Toolbar::class.java)),
                anyOf(withText(titleMatcher), withContentDescription(titleMatcher))
            )
        )
        private val overflowInteraction = Espresso.onView(withText(titleMatcher))
            .inRoot(RootMatchers.isPlatformPopup()) // Overflow menu is shown in system PopupWindow
        private val toolbarInteraction = Espresso.onView(toolbarMatcher)

        override fun perform(vararg actions: ViewAction) {
            val interceptedActions = actions.map { action ->
                ActionInterceptor.Proxy(
                    action,
                    UITestConfig.actionInterceptors
                )
            }

            if (isItemInOverflow()) {
                // do not click if disableOverflowMenuAutoOpen() was specified
                // TODO: try to use Espresso.openActionBarOverflowOrOptionsMenu() to open the menu
                overflowMenuButton?.click()
                overflowInteraction.waitToPerform(interceptedActions)
            } else {
                actionInteraction.waitToPerform(interceptedActions)
            }
        }

        override fun check(assertion: ViewAssertion) {
            if (isItemInOverflow()) {
                overflowMenuButton?.click()
                overflowInteraction.check(assertion)
                Device.pressBack()
            } else {
                actionInteraction.check(assertion)
            }
        }

        private fun isItemInOverflow(): Boolean {
            var inOverflow = false
            waitFor(allowedExceptions = setOf(IllegalStateException::class.java)) {
                inOverflow = ToolbarReadMenuItemsAction()
                    .apply { toolbarInteraction.perform(this) }
                    .hasHiddenItem(titleMatcher)
            }
            return inOverflow
        }
    }
}

interface ToolbarElementChecks : Checks {

    fun withTitle(text: String)

    fun withTitle(@StringRes resId: Int)

    fun withSubtitle(text: String)

    fun withSubtitle(@StringRes resId: Int)
}

class ToolbarElementChecksImpl(
    private val driver: ChecksDriver
) : ToolbarElementChecks,
    Checks by ChecksImpl(driver) {

    override fun withTitle(text: String) {
        driver.check(ViewAssertions.matches(ToolbarTitleMatcher(`is`(text))))
    }

    override fun withTitle(@StringRes resId: Int) {
        driver.check(ViewAssertions.matches(ToolbarTitleResMatcher(resId)))
    }

    override fun withSubtitle(text: String) {
        driver.check(ViewAssertions.matches(ToolbarSubtitleMatcher(`is`(text))))
    }

    override fun withSubtitle(@StringRes resId: Int) {
        driver.check(ViewAssertions.matches(ToolbarSubTitleResMatcher(resId)))
    }
}
