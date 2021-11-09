package com.avito.android.ui.test

import androidx.test.espresso.NoMatchingViewException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.PageObjectActivity
import com.avito.android.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageObjectTest {

    @get:Rule
    val rule = screenRule<PageObjectActivity>()

    @Suppress("DEPRECATION")
    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun element__found__byDefaultMatcher() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_1))

        screen().textView.checks.isDisplayed()
    }

    @Test
    fun element__found__byDefaultMatcherWithCustom() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_1))

        screen().textViewWithText.checks.isDisplayed()
    }

    @Test
    fun element__notFound__byDefaultMatcherWithWrongCustom() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_1))

        exception.expect(NoMatchingViewException::class.java)

        screen().textViewWithWrongText.checks.isDisplayed()
    }

    @Test
    fun parent__assertionError__byDefaultMatcherInTheWrongParent() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_2)) // twin

        exception.expect(junit.framework.AssertionFailedError::class.java)

        screen().textView.checks.isDisplayed()
    }

    @Test
    fun overridden_is_screen_opened_doesnt_have_recursion() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_1))

        screen().textView.checks.isDisplayed()
        screen().checks.isScreenOpened()
    }

    private fun screen() = PageObjectScreen(R.id.page_object_root_1)
}
