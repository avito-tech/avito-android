package com.avito.android.test.espresso.assertion

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`

class ViewExistsAssertion : ViewAssertion {

    override fun check(view: View?, noView: NoMatchingViewException?) {
        if (view == null) {
            assertThat(
                "View is not present in the hierarchy: ${noView?.viewMatcherDescription}",
                false,
                `is`(true)
            )
        }
    }
}
