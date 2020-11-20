package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.avito.android.test.matcher.CanBeLongClickedMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class ActionOnLongClickableElement(
    private val action: ViewAction
) : ViewAction {

    override fun getDescription(): String = "${action.description} on long-clickable element"

    override fun getConstraints(): Matcher<View> = Matchers.allOf(
        CanBeLongClickedMatcher(),
        action.constraints
    )

    override fun perform(uiController: UiController, view: View) {
        action.perform(uiController, view)
    }
}
