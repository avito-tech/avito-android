package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class ActionOnEnabledElement(
    private val action: ViewAction
) : ViewAction {

    override fun getDescription(): String = "${action.description} on enabled element"

    override fun getConstraints(): Matcher<View> = Matchers.allOf(
        ViewMatchers.isEnabled(),
        action.constraints
    )

    override fun perform(uiController: UiController, view: View) {
        action.perform(uiController, view)
    }
}
