package com.avito.android.test.page_object

import androidx.test.espresso.assertion.ViewAssertions
import com.avito.android.test.InteractionContext
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.matcher.BottomSheetCollapsedMatcher
import com.avito.android.test.matcher.BottomSheetExpandedMatcher

open class BottomSheetElement(interactionContext: InteractionContext) : ViewElement(interactionContext) {

    override val checks = ElementChecks(interactionContext)

    inner class ElementChecks(private val drivenChecks: ChecksDriver) : Checks by ChecksImpl(drivenChecks) {

        fun isExpanded() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetExpandedMatcher()))
        }

        fun isCollapsed() {
            drivenChecks.check(ViewAssertions.matches(BottomSheetCollapsedMatcher()))
        }
    }
}
