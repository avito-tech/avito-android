package com.avito.android.test.report

import android.view.View
import androidx.test.espresso.FailureHandler
import org.hamcrest.Matcher

object StubFailureHandler : FailureHandler {
    override fun handle(error: Throwable, viewMatcher: Matcher<View>?) {
        throw error
    }
}
