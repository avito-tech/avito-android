package com.avito.android.test.checks

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.LayoutMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.avito.android.test.matcher.CompoundDrawableMatcher
import com.avito.android.test.matcher.TextViewLinesMatcher
import com.avito.android.test.matcher.WithHintEndingMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.hamcrest.core.AllOf

class LabelChecksImpl(private val driver: ChecksDriver) : LabelChecks {

    override fun withText(text: String) {
        driver.check(matches(ViewMatchers.withText(text)))
    }

    override fun withText(textResId: Int) {
        driver.check(matches(ViewMatchers.withText(textResId)))
    }

    override fun withText(matcher: Matcher<String>) {
        driver.check(matches(ViewMatchers.withText(matcher)))
    }

    override fun withTextStartingWith(text: String) {
        driver.check(matches(ViewMatchers.withText(startsWith(text))))
    }

    override fun withTextIgnoringCase(text: String) {
        driver.check(matches(ViewMatchers.withText(equalToIgnoringCase(text))))
    }

    override fun withEmptyText() {
        driver.check(matches(ViewMatchers.withText("")))
    }

    override fun containsText(text: String) {
        driver.check(matches(ViewMatchers.withText(containsString(text))))
    }

    override fun displayedWithText(text: String) {
        displayedAndMatchedWith(ViewMatchers.withText(text))
    }

    override fun displayedWithText(textResId: Int) {
        displayedAndMatchedWith(ViewMatchers.withText(textResId))
    }

    override fun displayedWithText(matcher: Matcher<String>) {
        displayedAndMatchedWith(ViewMatchers.withText(matcher))
    }

    override fun displayedWithTextStartingWith(text: String) {
        displayedAndMatchedWith(ViewMatchers.withText(startsWith(text)))
    }

    override fun displayedWithTextEndingWith(text: String) {
        displayedAndMatchedWith(ViewMatchers.withText(endsWith(text)))
    }

    override fun displayedWithTextIgnoringCase(text: String) {
        displayedAndMatchedWith(ViewMatchers.withText(equalToIgnoringCase(text)))
    }

    override fun displayedWithEmptyText() {
        displayedAndMatchedWith(ViewMatchers.withText(""))
    }

    override fun withLinesCount(count: Int) {
        driver.check(matches(TextViewLinesMatcher(`is`(count))))
    }

    override fun withLinesCount(matcher: Matcher<Int>) {
        driver.check(matches(TextViewLinesMatcher(matcher)))
    }

    override fun endsWithHint(hint: String) {
        driver.check(matches(WithHintEndingMatcher(hint)))
    }

    override fun hasEllipsizedText() {
        driver.check(matches(LayoutMatchers.hasEllipsizedText()))
    }

    override fun hasNotEllipsizedText() {
        driver.check(matches(not(LayoutMatchers.hasEllipsizedText())))
    }

    override fun hasTag(tag: Any) {
        displayedAndMatchedWith(withTagValue(equalTo(tag)))
    }

    override fun withIcons(
        @DrawableRes left: Int?,
        @DrawableRes top: Int?,
        @DrawableRes right: Int?,
        @DrawableRes bottom: Int?,
        @ColorInt tint: Int?
    ) {
        driver.check(
            matches(
                CompoundDrawableMatcher(
                    left,
                    top,
                    right,
                    bottom,
                    tint
                )
            )
        )
    }

    private fun displayedAndMatchedWith(matcher: Matcher<View>) {
        driver.check(
            matches(
                AllOf.allOf(
                    isDisplayed(),
                    matcher
                )
            )
        )
    }
}
