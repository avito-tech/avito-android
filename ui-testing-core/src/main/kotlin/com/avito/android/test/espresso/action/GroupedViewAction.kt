package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class GroupedViewAction(
    private val actions: List<ViewAction>
) : ViewAction {

    override fun getDescription(): String = buildString {
        append("Grouped view action of:")
        actions.forEach {
            append(" ")
            append(it.description)
        }
    }

    override fun getConstraints(): Matcher<View> = Matchers.allOf(actions.map { it.constraints })

    override fun perform(uiController: UiController, view: View) {
        actions.forEach { it.perform(uiController, view) }
    }
}