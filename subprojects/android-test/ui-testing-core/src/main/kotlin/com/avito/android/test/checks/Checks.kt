package com.avito.android.test.checks

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

public interface Checks : LabelChecks {

    /**
     * Accepts a view so long as a given percentage of that view's area is
     * not obscured by any other view and is thus visible to the user.
     *
     * @param areaPercentage an integer ranging from (0, 100] indicating how much percent of the
     *   surface area of the view must be shown to the user to be accepted.
     */
    public fun isDisplayingAtLeast(areaPercentage: Int)

    /**
     * Accepts a view whose height and width fit perfectly within
     * the currently displayed region of this view.
     *
     * There exist views (such as ScrollViews) whose height and width are larger then the physical
     * device screen by design. Such views will _never_ be completely displayed.
     */
    public fun isCompletelyDisplayed()

    /**
     * Matches [View]s that are currently displayed on the screen to the
     * user.
     *
     * Note: isDisplayed will select views that are partially displayed (eg: the full height/width of
     * the view is greater then the height/width of the visible rectangle). If you wish to ensure the
     * entire rectangle this view draws is displayed to the user use isCompletelyDisplayed.
     */
    public fun isDisplayed()

    /**
     * Matches [View]s that are NOT currently displayed on the screen to the
     * user.
     *
     * Note: isNotDisplayed inverts the logic by isDisplayed().
     */
    public fun isNotDisplayed()

    /**
     * Accepts a [View] that overlapped with another view by at least one pixel
     */
    public fun isOverlapped()

    /**
     * Accepts a [View] that not overlapped with another view even by one pixel
     */
    public fun isNotOverlapped()

    // checked the property Visible of the PageObject and his parent hierarchy
    public fun isVisible()

    public fun isNotVisible()

    public fun hasVisibility(visibility: ViewMatchers.Visibility)

    public fun isEnabled()

    public fun isDisabled()

    /**
     * You can use it to check if a tab is active
     */
    public fun isSelected()

    public fun withChildCount(countMatcher: Matcher<Int>)

    public fun withChildCountEquals(count: Int)

    /**
     * Checking that element does not exist by matcher is fragile.
     * Positive scenario for the expected state is more reliable
     */
    public fun doesNotExist()

    public fun exists()

    public fun isChecked()

    public fun isNotChecked()

    /**
     * Asserts that view displayed is completely left of
     * the view matching the given matcher.
     */
    public fun isLeftOf(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely right of
     * the view matching the given matcher.
     */
    public fun isRightOf(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely aligned
     * to the left as the view matching the given matcher.
     *
     * <p>The left 'x' coordinate of the view displayed must equal the left 'x'
     * coordinate of the view matching the given matcher.
     */
    public fun isLeftAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely aligned
     * to the right as the view matching the given matcher.
     *
     * <p>The right 'x' coordinate of the view displayed must equal the right 'x'
     * coordinate of the view matching the given matcher.
     */
    public fun isRightAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely above
     * the view matching the given matcher.
     */
    public fun isAbove(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * below the view matching the given matcher.
     */
    public fun isBelow(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * aligned to bottom with the view matching the given matcher.
     *
     * The bottom 'y' coordinate of the view displayed must equal the bottom 'y'
     * coordinate of the view matching the given matcher.
     */
    public fun isBottomAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * aligned to top with the view matching the given matcher.
     *
     * The top 'y' coordinate of the view displayed must equal the top 'y'
     * coordinate of the view matching the given matcher.
     */
    public fun isTopAlignedWith(matcher: Matcher<View>)

    public fun isCenteredVerticallyWith(matcher: Matcher<View>)

    public fun isCenteredVerticallyWithBottomOf(matcher: Matcher<View>)

    public fun isDisplayedWithAlpha(alpha: Float)

    public fun isFocusable()

    public fun isNotFocusable()

    public fun isFocusableInTouchMode()

    public fun isNotFocusableInTouchMode()

    /**
     * Accepts a view whose height and width fit *perfectly outside* of the currently displayed region of this view.
     */
    public fun isNotCompletelyDisplayed()

    public fun isClickable()

    public fun isNotClickable()

    public fun hasFocus()

    public fun hasBackground(@DrawableRes drawable: Int? = null, @ColorRes tint: Int? = null)
}
