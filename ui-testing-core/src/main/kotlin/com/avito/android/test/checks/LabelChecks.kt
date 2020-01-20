package com.avito.android.test.checks

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.hamcrest.Matcher

interface LabelChecks {

    fun withText(text: String)

    fun withText(@StringRes textResId: Int)

    fun withText(matcher: Matcher<String>)

    fun displayedWithText(text: String)

    fun displayedWithText(@StringRes textResId: Int)

    fun displayedWithText(matcher: Matcher<String>)

    fun withTextStartingWith(text: String)

    fun displayedWithTextStartingWith(text: String)

    fun displayedWithTextEndingWith(text: String)

    fun withTextIgnoringCase(text: String)

    fun displayedWithTextIgnoringCase(text: String)

    fun withEmptyText()

    fun containsText(text: String)

    fun displayedWithEmptyText()

    fun withLinesCount(count: Int)

    fun withLinesCount(matcher: Matcher<Int>)

    fun endsWithHint(hint: String)

    fun hasEllipsizedText()

    fun hasNotEllipsizedText()

    fun hasTag(tag: Any)

    fun withIcons(
        @DrawableRes left: Int? = null,
        @DrawableRes top: Int? = null,
        @DrawableRes right: Int? = null,
        @DrawableRes bottom: Int? = null,
        @ColorInt tint: Int? = null
    )
}
