package com.avito.android.ui.test

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.R
import com.avito.android.ui.VisibilityActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun hasVisibility_visible_forVisibleElement() {
        rule.launchActivity(null)

        Screen.visibility.label.checks.hasVisibility(ViewMatchers.Visibility.VISIBLE)
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
