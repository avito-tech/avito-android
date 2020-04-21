package com.avito.android.ui.test

import androidx.test.espresso.NoMatchingViewException
import com.avito.android.ui.PageObjectActivity
import com.avito.android.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class PageObjectTest {

    @get:Rule
    val rule = screenRule<PageObjectActivity>()

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
    fun element__notFound__byDefaultMatcherInTheWrongParent() {
        rule.launchActivity(PageObjectActivity.intent(R.layout.page_object_2)) // twin

        exception.expect(NoMatchingViewException::class.java)

        screen().textView.checks.isDisplayed()
    }

    private fun screen() = PageObjectScreen(R.id.page_object_root_1)

}
