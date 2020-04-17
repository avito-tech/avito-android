package com.avito.android.test

import android.view.View
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class VisibilityTest {

    @get:Rule
    val rule = screenRule<VisibilityActivity>()

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun isVisible_success_forVisibleElement() {
        rule.launchActivity(null)

        Screen.visibility.label.checks.isVisible()
    }

    @Test
    fun isVisible_fail_forInVisibleElement() {
        rule.launchActivity(null)

        rule.runOnUiThread {
            rule.activity.findViewById<View>(R.id.text).visibility = View.INVISIBLE
        }
        exception.expectMessage(
            "'view has effective visibility=VISIBLE' " +
                "doesn't match the selected view."
        )
        Screen.visibility.label.checks.isVisible()
    }
}
