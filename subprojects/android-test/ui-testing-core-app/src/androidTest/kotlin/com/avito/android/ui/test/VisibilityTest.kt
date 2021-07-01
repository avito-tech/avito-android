package com.avito.android.ui.test

import android.view.View
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.R
import com.avito.android.ui.VisibilityActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class VisibilityTest {

    @get:Rule
    val rule = screenRule<VisibilityActivity>()

    @Suppress("DEPRECATION")
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

        rule.onActivity {
            findViewById<View>(R.id.text).visibility = View.INVISIBLE
        }
        exception.expectMessage(
            "'view has effective visibility <VISIBLE>' " +
                "doesn't match the selected view."
        )
        Screen.visibility.label.checks.isVisible()
    }
}
