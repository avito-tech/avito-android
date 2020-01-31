package com.avito.android.test.matcher

import android.content.res.Resources
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

internal class ToolbarTitleResMatcher(
    @StringRes private val resourceId: Int
) : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

    private var resourceName: String? = null
    private var expectedText: String? = null
    private var actualText: String? = null

    override fun describeTo(description: Description) {
        description.appendText("with toolbar from resourceId: $resourceId")
        when {
            resourceName != null -> description.appendText(" resourceName = $resourceName")
            expectedText != null -> description.appendText(" expectedValue = $expectedText")
            actualText != null -> description.appendText(" actualText = $actualText")
        }
    }

    override fun matchesSafely(toolbar: Toolbar): Boolean {
        try {
            with(toolbar.resources) {
                resourceName = getResourceName(resourceId)
                expectedText = getString(resourceId)
                expectedText
            }
        } catch (ignored: Resources.NotFoundException) {
            /* view could be from a context unaware of the resource id. */
        }

        actualText = toolbar.title.toString()

        return expectedText == actualText
    }
}
