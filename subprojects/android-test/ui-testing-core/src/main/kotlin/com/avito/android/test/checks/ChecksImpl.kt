package com.avito.android.test.checks

import android.view.View
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import com.avito.android.test.espresso.assertion.ViewExistsAssertion
import com.avito.android.test.matcher.AvitoPositionAssertions
import com.avito.android.test.matcher.DrawableBackgroundMatcher
import com.avito.android.test.matcher.OverlapMatcher
import com.avito.android.test.matcher.ViewGroupMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

class ChecksImpl(private val driver: ChecksDriver) : Checks,
    LabelChecks by LabelChecksImpl(driver) {

    override fun isDisplayingAtLeast(areaPercentage: Int) {
        driver.check(matches(ViewMatchers.isDisplayingAtLeast(areaPercentage)))
    }

    override fun isCompletelyDisplayed() {
        driver.check(matches(ViewMatchers.isCompletelyDisplayed()))
    }

    override fun isDisplayed() {
        driver.check(matches(ViewMatchers.isDisplayed()))
    }

    override fun isNotDisplayed() {
        driver.check(matches(not(ViewMatchers.isDisplayed())))
    }

    override fun isOverlapped() {
        driver.check(matches(OverlapMatcher()))
    }

    override fun isNotOverlapped() {
        driver.check(matches(not(OverlapMatcher())))
    }

    override fun isVisible() {
        hasVisibility(Visibility.VISIBLE)
    }

    override fun isNotVisible() {
        driver.check(matches(not(ViewMatchers.withEffectiveVisibility(Visibility.VISIBLE))))
    }

    override fun hasVisibility(visibility: Visibility) {
        driver.check(matches(ViewMatchers.withEffectiveVisibility(visibility)))
    }

    override fun isEnabled() {
        driver.check(matches(ViewMatchers.isEnabled()))
    }

    override fun isDisabled() {
        driver.check(matches(not(ViewMatchers.isEnabled())))
    }

    override fun isSelected() {
        driver.check(matches(ViewMatchers.isSelected()))
    }

    override fun withChildCount(countMatcher: Matcher<Int>) {
        driver.check(matches(ViewGroupMatcher().hasChildren(countMatcher)))
    }

    override fun withChildCountEquals(count: Int) {
        driver.check(matches(ViewGroupMatcher().hasChildren(`is`(count))))
    }

    override fun doesNotExist() {
        driver.check(ViewAssertions.doesNotExist())
    }

    override fun exists() {
        driver.check(ViewExistsAssertion())
    }

    override fun isChecked() {
        driver.check(matches(ViewMatchers.isChecked()))
    }

    override fun isNotChecked() {
        driver.check(matches(ViewMatchers.isNotChecked()))
    }

    override fun isLeftOf(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isCompletelyLeftOf(matcher))
    }

    override fun isRightOf(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isCompletelyRightOf(matcher))
    }

    override fun isLeftAlignedWith(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isLeftAlignedWith(matcher))
    }

    override fun isRightAlignedWith(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isRightAlignedWith(matcher))
    }

    override fun isAbove(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isCompletelyAbove(matcher))
    }

    override fun isBelow(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isCompletelyBelow(matcher))
    }

    override fun isBottomAlignedWith(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isBottomAlignedWith(matcher))
    }

    override fun isTopAlignedWith(matcher: Matcher<View>) {
        driver.check(PositionAssertions.isTopAlignedWith(matcher))
    }

    override fun isCenteredVerticallyWith(matcher: Matcher<View>) {
        driver.check(AvitoPositionAssertions.isCenteredVerticallyWith(matcher))
    }

    override fun isCenteredVerticallyWithBottomOf(matcher: Matcher<View>) {
        driver.check(AvitoPositionAssertions.isCenteredVerticallyWithBottomOf(matcher))
    }

    override fun isDisplayedWithAlpha(alpha: Float) {
        driver.check(matches(com.avito.android.test.matcher.ViewMatchers.hasAlpha(alpha)))
    }

    override fun isFocusable() {
        driver.check(matches(ViewMatchers.isFocusable()))
    }

    override fun isNotFocusable() {
        driver.check(matches(not(ViewMatchers.isFocusable())))
    }

    override fun hasFocus() {
        driver.check(matches(ViewMatchers.hasFocus()))
    }

    override fun isFocusableInTouchMode() {
        driver.check(
            matches(
                com.avito.android.test.matcher.ViewMatchers.isFocusableInTouchMode()
            )
        )
    }

    override fun isNotFocusableInTouchMode() {
        driver.check(
            matches(
                not(
                    com.avito.android.test.matcher.ViewMatchers.isFocusableInTouchMode()
                )
            )
        )
    }

    override fun isNotCompletelyDisplayed() {
        driver.check(matches(not(ViewMatchers.isCompletelyDisplayed())))
    }

    override fun isClickable() {
        driver.check(matches(ViewMatchers.isClickable()))
    }

    override fun isNotClickable() {
        driver.check(matches(not(ViewMatchers.isClickable())))
    }

    override fun hasBackground(drawable: Int?, tint: Int?) {
        driver.check(
            matches(
                DrawableBackgroundMatcher(
                    src = drawable,
                    tint = tint
                )
            )
        )
    }
}
