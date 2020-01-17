package com.avito.android.test.checks

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.hamcrest.Matcher

interface Checks : LabelChecks {

    /**
     * Accepts a view so long as a given percentage of that view's area is
     * not obscured by any other view and is thus visible to the user.
     *
     * @param areaPercentage an integer ranging from (0, 100] indicating how much percent of the
     *   surface area of the view must be shown to the user to be accepted.
     */
    fun isDisplayingAtLeast(areaPercentage: Int)

    /**
     * Accepts a view whose height and width fit perfectly within
     * the currently displayed region of this view.
     *
     * There exist views (such as ScrollViews) whose height and width are larger then the physical
     * device screen by design. Such views will _never_ be completely displayed.
     */
    fun isCompletelyDisplayed()

    /**
     * Matches {@link View}s that are currently displayed on the screen to the
     * user.
     *
     * Note: isDisplayed will select views that are partially displayed (eg: the full height/width of
     * the view is greater then the height/width of the visible rectangle). If you wish to ensure the
     * entire rectangle this view draws is displayed to the user use isCompletelyDisplayed.
     */
    fun isDisplayed()

    /**
     * Matches {@link View}s that are NOT currently displayed on the screen to the
     * user.
     *
     * Note: isNotDisplayed inverts the logic by isDisplayed().
     */
    fun isNotDisplayed()

    // checked the property Visible of the PageObject and his parent hierarchy
    fun isVisible()

    fun isNotVisible()

    fun isEnabled()

    fun isDisabled()

    /**
     * Можно использовать для проверки "выбранности" табов
     */
    fun isSelected()

    fun withChildCount(countMatcher: Matcher<Int>)

    fun withChildCountEquals(count: Int)

    @Deprecated(message = "Проверка отсутсвия элемента по какому-то матчеру хрупкая. " +
        "Позитивный сценарий на ожидаемое состояние более надежен")
    fun doesNotExist()

    fun exists()

    fun isChecked()

    fun isNotChecked()

    /**
     * Asserts that view displayed is completely left of
     * the view matching the given matcher.
     */
    fun isLeftOf(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely right of
     * the view matching the given matcher.
     */
    fun isRightOf(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely aligned
     * to the left as the view matching the given matcher.
     *
     * <p>The left 'x' coordinate of the view displayed must equal the left 'x'
     * coordinate of the view matching the given matcher.
     */
    fun isLeftAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely aligned
     * to the right as the view matching the given matcher.
     *
     * <p>The right 'x' coordinate of the view displayed must equal the right 'x'
     * coordinate of the view matching the given matcher.
     */
    fun isRightAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely above
     * the view matching the given matcher.
     */
    fun isAbove(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * below the view matching the given matcher.
     */
    fun isBelow(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * aligned to bottom with the view matching the given matcher.
     *
     * The bottom 'y' coordinate of the view displayed must equal the bottom 'y'
     * coordinate of the view matching the given matcher.
     */
    fun isBottomAlignedWith(matcher: Matcher<View>)

    /**
     * Asserts that view displayed is completely
     * aligned to top with the view matching the given matcher.
     *
     * The top 'y' coordinate of the view displayed must equal the top 'y'
     * coordinate of the view matching the given matcher.
     */
    fun isTopAlignedWith(matcher: Matcher<View>)

    fun isCenteredVerticallyWith(matcher: Matcher<View>)

    fun isCenteredVerticallyWithBottomOf(matcher: Matcher<View>)

    fun isDisplayedWithAlpha(alpha: Float)

    fun isFocusable()

    fun isNotFocusable()

    fun isFocusableInTouchMode()

    fun isNotFocusableInTouchMode()

    /**
     * Accepts a view whose height and width fit *perfectly outside* of the currently displayed region of this view.
     */
    fun isNotCompletelyDisplayed()

    fun isClickable()

    fun isNotClickable()

    fun hasFocus()

    fun hasBackground(@DrawableRes drawable: Int? = null, @ColorRes tint: Int? = null)
}
