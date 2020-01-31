package com.avito.android.test.matcher

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * Implementation from ViewMatchers, with custom error message
 */
class IsAssignableFromMatcher(
    private val clazz: Class<out View>,
    private val errorMessage: (Class<out View>) -> String = { "is assignable from class: $it" }
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText(errorMessage(clazz))
    }

    override fun matchesSafely(view: View): Boolean = clazz.isAssignableFrom(view.javaClass)
}
