package com.avito.android.test.checks

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.hamcrest.Matcher

public interface LabelChecks {

    public fun withText(text: String)

    public fun withText(@StringRes textResId: Int)

    public fun withText(matcher: Matcher<String>)

    public fun displayedWithText(text: String)

    public fun displayedWithText(@StringRes textResId: Int)

    public fun displayedWithText(matcher: Matcher<String>)

    public fun withTextStartingWith(text: String)

    public fun displayedWithTextStartingWith(text: String)

    public fun displayedWithTextEndingWith(text: String)

    public fun withTextIgnoringCase(text: String)

    public fun displayedWithTextIgnoringCase(text: String)

    public fun withEmptyText()

    public fun containsText(text: String)

    public fun displayedWithEmptyText()

    public fun withLinesCount(count: Int)

    public fun withLinesCount(matcher: Matcher<Int>)

    public fun endsWithHint(hint: String)

    public fun hasEllipsizedText()

    public fun hasNotEllipsizedText()

    public fun hasTag(tag: Any)

    public fun withIcons(
        @DrawableRes left: Int? = null,
        @DrawableRes top: Int? = null,
        @DrawableRes right: Int? = null,
        @DrawableRes bottom: Int? = null,
        @ColorInt tint: Int? = null
    )
}
