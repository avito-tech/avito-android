package com.avito.android.rule

import android.view.View
import androidx.annotation.StringRes
import org.hamcrest.Matcher

public interface ToastChecks {

    public fun toastDisplayedWithText(text: String)

    public fun toastDisplayedWithText(@StringRes resId: Int)

    @Deprecated(
        "Use toastDisplayedWithView for custom views " +
            "or toastDisplayedWithText(String|StringRes) for simple toasts"
    )
    public fun toastDisplayedWithText(matcher: Matcher<View>)

    /**
     * Toasts with custom views
     */
    public fun toastDisplayedWithView(matcher: Matcher<View>)

    public fun toastNotDisplayed()
}
