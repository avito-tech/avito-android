package com.avito.android.test.page_object

import androidx.test.espresso.assertion.ViewAssertions
import android.view.View
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.checks.OnViewChecksDriver
import com.avito.android.test.matcher.BottomSheetCollapsedMatcher
import com.avito.android.test.matcher.BottomSheetExpandedMatcher
import org.hamcrest.Matcher

open class BottomSheetElement(matcher: Matcher<View>) : ViewElement(matcher) {

    override val checks = ElementChecks()

    inner class ElementChecks(
        private val drivenChecks: ChecksDriver = OnViewChecksDriver(matcher)
    ) : Checks by ChecksImpl(drivenChecks) {

        fun isExpanded() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetExpandedMatcher()))
        }

        fun isCollapsed() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetCollapsedMatcher()))
        }

    }
}
