package com.avito.android.ui.test

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.avito.android.test.InteractionContext
import com.avito.android.test.page_object.HandleParentContext
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class PageObjectScreen(@IdRes override val rootId: Int) : SimpleScreen() {

    /**
     * Our goal is to demonstrate how interaction context extends matcher in case of HandleParentContext
     *
     * We use a standalone parent element intentionally.
     * It creates an interaction context with the exact id in the root.
     * Unfortunately, PageObject.element doesn't respect Screen.rootId yet.
     * It checks current screen only by Screen.check.isOpen
     */
    private val parentContainer: ViewElement = element(withId(rootId))

    val textView: TextViewElement = parentContainer.element()

    val textViewWithText: TextViewElement = parentContainer.element(withText("text"))

    val textViewWithWrongText: TextViewElement = parentContainer.element(withText("invalid"))

    override val checks: ScreenChecks = ScreenChecks(this)

    class ScreenChecks(screen: PageObjectScreen) :
        SimpleScreenChecks<PageObjectScreen>(screen, checkOnEachScreenInteraction = true) {

        override fun screenOpenedCheck() {
            super.screenOpenedCheck()
            screen.parentContainer.checks.exists()
        }
    }
}

/**
 * Example of custom view element with default matcher
 */
@Suppress("unused")
class TextViewElement : HandleParentContext, ViewElement {

    constructor(
        interactionContext: InteractionContext
    ) : super(interactionContext.provideChildContext(defaultMatcher()))

    constructor(
        interactionContext: InteractionContext,
        matcher: Matcher<View>
    ) : super(
        interactionContext.provideChildContext(
            Matchers.allOf(
                defaultMatcher(),
                matcher
            )
        )
    )
}

private fun defaultMatcher(): Matcher<View> = ViewMatchers.isAssignableFrom(TextView::class.java)
